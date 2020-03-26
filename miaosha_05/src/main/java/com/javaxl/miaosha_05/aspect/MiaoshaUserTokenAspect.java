package com.javaxl.miaosha_05.aspect;

import com.javaxl.miaosha_05.annotation.DisableToken;
import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.exception.GlobalException;
import com.javaxl.miaosha_05.result.CodeMsg;
import com.javaxl.miaosha_05.service.MiaoshaUserService;
import com.javaxl.miaosha_05.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author 小李飞刀
 * @site www.javaxl.com
 * @company
 * @create  2019-12-08 9:10
 */
@Aspect
@Component
@Slf4j
public class MiaoshaUserTokenAspect {
    @Autowired
    MiaoshaUserService userService;

    @Pointcut("execution( * com.javaxl..controller.*.*(..))")
    public void miaoshaUserTokenCut() {
    }

    @Around("miaoshaUserTokenCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();

        Signature s = pjp.getSignature();
        MethodSignature ms = (MethodSignature) s;
        Method m = ms.getMethod();
        Annotation[] annotations = m.getAnnotations();
        for (Annotation annotation : annotations) {
            //如果在方法上添加了DisableToken注解，那么此方法是不需要token令牌就能访问的
            if (annotation instanceof DisableToken) {
                //直接放行
                return pjp.proceed(args);
            }
        }

        int count = 0;
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        MiaoshaUser miaoshaUser = null;
        //主要是对参数数组的中的秒杀User做封装处理
        int loop = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                count++;
                request = (HttpServletRequest) args[i];
            } else if (args[i] instanceof HttpServletResponse) {
                count++;
                response = (HttpServletResponse) args[i];
            } else if (args[i] instanceof MiaoshaUser) {
                count++;
                miaoshaUser = (MiaoshaUser) args[i];
                loop = i;
            }
        }

        if (count == 3) {
            String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
            String cookieToken = UserContext.getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
            //如果前端没传token过来
            if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
                throw new GlobalException(CodeMsg.SESSION_ERROR);
            }
            String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
            //根据token从缓存取用户信息
            miaoshaUser = userService.getByToken(response, token);
            args[loop] = miaoshaUser;
        }
        Object ob = pjp.proceed(args);//ob为方法的返回值
        return ob;
    }
}
