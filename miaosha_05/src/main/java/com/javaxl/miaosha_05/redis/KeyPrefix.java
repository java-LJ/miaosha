package com.javaxl.miaosha_05.redis;

/**
 * 做缓存的前缀接口
 */
public interface KeyPrefix {
		
	public int expireSeconds();
	
	public String getPrefix();
}
