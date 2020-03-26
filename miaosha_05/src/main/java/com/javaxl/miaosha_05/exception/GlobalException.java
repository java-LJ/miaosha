package com.javaxl.miaosha_05.exception;

import com.javaxl.miaosha_05.result.CodeMsg;

/**
 * 自定义异常类继承RuntimeException（运行时异常）
 */
public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}
}
