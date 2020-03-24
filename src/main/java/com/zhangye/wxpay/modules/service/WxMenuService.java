package com.zhangye.wxpay.modules.service;

import com.zhangye.wxpay.modules.model.Order;

/**
 * @author zhangye
 * @version 1.0
 * @description 微信支付接口类
 * @date 2019/12/19
 */
public interface WxMenuService {


    /**
     * 生成支付二维码URL
     *
     * @param order    订单类
     * @param signType 签名类型
     * @throws Exception
     */
    String wxPayUrl(Order order, String signType) throws Exception;

    /**
     * 查询微信订单
     *
     * @param orderNo  订单号
     * @param signType 签名类型
     * @return
     */
    String wxOrderQuery(String orderNo, String signType) throws Exception;

    /**
     * 关闭微信支付订单
     *
     * @param orderNo  订单号
     * @param signType 签名类型
     * @return
     */
    String wxCloseOrder(String orderNo, String signType) throws Exception;
}
