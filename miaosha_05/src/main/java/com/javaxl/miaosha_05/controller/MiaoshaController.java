package com.javaxl.miaosha_05.controller;

import com.javaxl.miaosha_05.annotation.AccessLimit;
import com.javaxl.miaosha_05.domain.MiaoshaOrder;
import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.rabbitmq.MQSender;
import com.javaxl.miaosha_05.rabbitmq.MiaoshaMessage;
import com.javaxl.miaosha_05.redis.GoodsKey;
import com.javaxl.miaosha_05.redis.MiaoshaKey;
import com.javaxl.miaosha_05.redis.OrderKey;
import com.javaxl.miaosha_05.redis.RedisService;
import com.javaxl.miaosha_05.result.CodeMsg;
import com.javaxl.miaosha_05.result.Result;
import com.javaxl.miaosha_05.service.GoodsService;
import com.javaxl.miaosha_05.service.MiaoshaService;
import com.javaxl.miaosha_05.service.MiaoshaUserService;
import com.javaxl.miaosha_05.service.OrderService;
import com.javaxl.miaosha_05.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    private MQSender sender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 系统初始化后就将所有商品库存放入缓存
     */
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);//初始化每个商品都是false，就是还有库存
        }
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
                                              @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    /**
     * 隐藏原有的秒杀地址
     */
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path") String path) {
        model.addAttribute("user", user);
        //验证秒杀地址
        boolean checkPath = miaoshaService.checkPath(user, goodsId, path);
        if (!checkPath){
            return Result.error(CodeMsg.MIAOSHA_PATH_INVALID);
        }
        try {
            //内存标记，减少redis访问，从map中取出
            boolean over = localOverMap.get(goodsId);
            if (over) {
                return Result.error(CodeMsg.MIAO_SHA_OVER);
            }
            //预减库存：从缓存中减去库存
            //利用redis中的方法，减去库存，返回值为减去1之后的值
            long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
            //这里判断不能小于等于，因为减去之后等于0说明还有是正常范围
            if (stock < 0) {
                localOverMap.put(goodsId, true);
                //返回秒杀完毕
                return Result.error(CodeMsg.MIAO_SHA_OVER);
            }
            //判断是否已经秒杀到了，避免一个账户秒杀多个商品
            MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
            if (order != null) {
                //还原库存
                redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
                return Result.error(CodeMsg.REPEATE_MIAOSHA);
            }
        } catch (Exception e) {
            /**
             * 当最后一个商品下单出现错误时，数据库减少库存失败，redis减少库存成功
             * 这时就会出现库存售不完的情况，所以要将redis缓存还原，即redis库存数加1
             */
            redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }

        //将请求入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0);//排队中
    	/*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
        */
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        //清空用户已经秒杀过某商品的订单记录
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        //清空订单
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    /**
     * 获取秒杀的path，并且验证验证码的值是否正确
     * 加入注解，实现拦截功能，进而实现限流功能
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(MiaoshaUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //内存标记，减少redis访问，从map中取出
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存：从缓存中减去库存
        //利用redis中的方法，减去库存，返回值为减去1之后的值
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        //这里判断不能小于等于，因为减去之后等于0说明还有是正常范围
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            //还原库存
            redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
            //返回秒杀完毕
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            //还原库存
            redisService.incr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0);//排队中
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(HttpServletRequest request, HttpServletResponse response, Model
            model, MiaoshaUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }
}
