package com.javaxl.miaosha_05.util;

import com.javaxl.miaosha_05.domain.MiaoshaUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();

    public static void setUser(MiaoshaUser user) {
        userHolder.set(user);
    }

    public static MiaoshaUser getUser() {
        return userHolder.get();
    }

    public static String getCookieValue(HttpServletRequest request, String cookie1NameToken) {
        //遍历request里面所有的cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookie1NameToken)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}