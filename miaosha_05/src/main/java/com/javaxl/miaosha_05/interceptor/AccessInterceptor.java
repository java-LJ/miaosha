package com.javaxl.miaosha_05.interceptor;

import com.alibaba.fastjson.JSON;
import com.javaxl.miaosha_05.annotation.AccessLimit;
import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.redis.AccessKey;
import com.javaxl.miaosha_05.redis.RedisService;
import com.javaxl.miaosha_05.result.CodeMsg;
import com.javaxl.miaosha_05.result.Result;
import com.javaxl.miaosha_05.service.MiaoshaUserService;
import com.javaxl.miaosha_05.util.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    MiaoshaUserService miaoshaUserService;
    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            //先去取得用户做判断
            MiaoshaUser user = getUser(request, response);
            //将user保存下来
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit aclimit = hm.getMethodAnnotation(AccessLimit.class);
            //无该注解的时候，那么就不进行拦截操作
            if (aclimit == null) {
                return true;
            }
            //获取参数
            int seconds = aclimit.seconds();
            int maxCount = aclimit.maxCount();
            boolean needLogin = aclimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {//需要登录
                if (user == null) {
                    //需要给客户端一个提示
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }
            //限定key5s之内只能访问5次，动态设置有效期
            AccessKey akey = AccessKey.expire(seconds);
            Integer count = redisService.get(akey, key, Integer.class);
            if (count == null) {
                redisService.set(akey, key, 1);
            }
            else if (count < maxCount) {
                redisService.incr(akey, key);
            }
            else {//超过5次
                //返回结果给前端
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws IOException {
        //指定输出的编码格式，避免乱码
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String jres = JSON.toJSONString(Result.error(cm));
        out.write(jres.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = UserContext.getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        MiaoshaUser user = miaoshaUserService.getByToken(response, token);
        return user;
    }
}