package com.javaxl.miaosha_05.redis;

public class GoodsKey extends BasePrefix {

    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    //商品id（1分钟）
    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
    //描述（1分钟）
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
    //库存（不过期）
    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "gs");
}
