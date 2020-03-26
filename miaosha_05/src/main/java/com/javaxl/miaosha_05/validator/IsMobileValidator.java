package com.javaxl.miaosha_05.validator;

import com.javaxl.miaosha_05.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 注解校验器类：继承ConstraintValidator类<注解类，注解参数类型>，
 * 实现两个方法（initialize：初始化操作、isValid：逻辑处理）
 * IsMobile：自定义的注解
 * String：注解参数类型
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
    //默认值为false，用于接收注解上自定义的required属性
    private boolean required = false;

    //1、初始化方法：通过该方法可以拿到我们的注解
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    //2、逻辑处理
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required) {
            return ValidatorUtil.isMobile(value);
        }
        else {
            if(StringUtils.isEmpty(value)) {
                return true;
            }
            else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
