package com.javaxl.miaosha_05.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)//运行期间有效
@Target(ElementType.METHOD)//注解类型为方法注解
public @interface AccessLimit {
    int seconds(); //固定时间

    int maxCount();//最大访问次数

    boolean needLogin() default true;// 用户是否需要登录
}