package com.javaxl.miaosha_05.config;

import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.service.MiaoshaUserService;
import com.javaxl.miaosha_05.util.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//将UserArgumentResolver注册到config里面去
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    //既然能注入service，那么可以用来容器来管理，将其放在容器中
    @Autowired
    MiaoshaUserService miaoshaUserService;

    public Object resolveArgument(MethodParameter arg0, ModelAndViewContainer arg1, NativeWebRequest webRequest,
                                  WebDataBinderFactory arg3) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        //获取cookie
        String cookieToken = UserContext.getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        MiaoshaUser user = miaoshaUserService.getByToken(response, token);
        return user;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        //返回参数的类型
        Class<?> clazz = parameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }
}
