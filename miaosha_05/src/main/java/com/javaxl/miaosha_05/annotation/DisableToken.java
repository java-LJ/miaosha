package com.javaxl.miaosha_05.annotation;


import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author 小李飞刀
 * @site www.javaxl.com
 * @company
 * @create  2019-12-08 11:09
 *
 * 作用在方法上，以及类上
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
@Inherited
public @interface DisableToken {
}
