package com.javaxl.miaosha_05.service;

import com.javaxl.miaosha_05.dao.MiaoshaUserDao;
import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.exception.GlobalException;
import com.javaxl.miaosha_05.redis.MiaoshaUserKey;
import com.javaxl.miaosha_05.redis.RedisService;
import com.javaxl.miaosha_05.result.CodeMsg;
import com.javaxl.miaosha_05.shiro.PasswordHelper;
import com.javaxl.miaosha_05.util.MD5Util;
import com.javaxl.miaosha_05.util.UUIDUtil;
import com.javaxl.miaosha_05.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Service
public class MiaoshaUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (user != null) {
            return user;
        }
        //取数据库
        user = miaoshaUserDao.getById(id);
        if (user != null) {
            //放缓存
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        }
        return user;
    }

    /**
     * 注意数据修改时候，保持缓存与数据库的一致性
     */
    public boolean updatePassword(String token,long id,String passNew) {
        //1.取user对象，查看是否存在
        MiaoshaUser user=getById(id);
        if(user==null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //2.更新密码
        MiaoshaUser toupdateuser=new MiaoshaUser();
        toupdateuser.setId(id);
        toupdateuser.setPassword(MD5Util.inputPassToDbPass(passNew, user.getSalt()));
        miaoshaUserDao.update(toupdateuser);
        //3.更新数据库与缓存，一定保证数据一致性，修改token关联的对象以及id关联的对象
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        //不能直接删除token，删除之后就不能登录了
        user.setPassword(toupdateuser.getPassword());
        redisService.set(MiaoshaUserKey.token, token,user);
        return true;
    }

    /**
     * 从缓存里面取得值，取得value
     */
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        if (!PasswordHelper.checkCredentials(formPass, saltDB, dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    /**
     * 添加或者更新cookie
     */
    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        /**
         * 将token写到cookie当中，然后传递给客户端
         * 此token对应的是一个用户,将用户信息存放到一个第三方的缓存中
         * prefix --> MiaoshaUserKey.token
         * key --> token
         * value --> 用户的信息
         * 然后拿到了token就可以知道对应的用户信息
         */
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        //设置cookie的有效期，与session有效期一致
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        //设置网站的根目录
        cookie.setPath("/");
        //需要写到response中
        response.addCookie(cookie);
    }
}
