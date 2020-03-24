package com.zhangye.wxpay.modules.model;

import java.io.Serializable;

/**
 * @author zhangye
 * @version 1.0
 * @description 商户订单实体类(测试)
 * @date 2019/12/19
 */
public class Order implements Serializable {
    private String productId;//商品id
    private String subject;//商品名称
    private String orderNo;//订单号
    private String clintIp;//客户端ip
    private int totalFee;//订单金额 以分为单位

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getClintIp() {
        return clintIp;
    }

    public void setClintIp(String clintIp) {
        this.clintIp = clintIp;
    }

    public int getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(int totalFee) {
        this.totalFee = totalFee;
    }
}
