package com.zhangye.wxpay.modules.common.wx;

/**
 * @author zhangye
 * @version 1.0
 * @description 微信公众号常量类
 * @date 2019/12/19
 */
public class IoPayConstants {

    /**
     * 默认编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 统一下单-扫描支付
     */
    public static String PAY_UNIFIEDORDER = "https://pay.4jicao.com/api/pay/create_order";

    /**
     * 统一下单-查询订单
     */
    public static String PAY_ORDERQUERY = "http://pay.4jicao.com/api/pay/query_order";

    /**
     * 统一下单-关闭订单
     */
    public static String PAY_CLOSEORDER = "https://api.mch.weixin.qq.com/pay/closeorder";

    /**
     * 请求成功返回码
     */
    public final static String ERRCODE_OK_CODE = "0";
    /**
     * 错误的返回码的Key
     */
    public final static String ERRCODE = "errcode";

    /**
     * 返回状态码
     */
    public final static String RETURN_CODE = "retCode";
    
    /**
     * 返回状态码
     */
    public final static String RES_CODE = "resCode";

    /**
     * access_token 字符串
     */
    public final static String ACCESS_TOKEN = "access_token";

    /**
     * 签名类型 MD5
     */
    public final static String SING_MD5 = "MD5";

    /**
     * 签名类型 HMAC-SHA256
     */
    public final static String SING_HMACSHA256 = "HMAC-SHA256";

}
