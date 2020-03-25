package com.zhangye.wxpay.modules.common.wx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IotopayConfig {
    /**
     * 开发者ID
     */
    public static String mchId;
    @Value("${iotopay.mchId}")
    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    /**
     * 开发者密码
     */
    public static String mchKey;
    @Value("${iotopay.mchKey}")
    public void setchKey(String mchKey) {
        this.mchKey = mchKey;
    }

    /**
     * 商户号
     */
    public static String jobNo;
    @Value("${iotopay.jobNo}")
    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }


    /**
     * 统一下单-通知链接
     */
    public static String unifiedorderNotifyUrl;
    @Value("${iotopay.unifiedorder.notifyUrl}")
    public void setUnifiedorderNotifyUrl(String unifiedorderNotifyUrl) {
        this.unifiedorderNotifyUrl = unifiedorderNotifyUrl;
    }

    //支付map缓存处理
    private static HashMap<String,String> payMap = new HashMap<String,String>();
    public static String getPayMap(String key) {
        return payMap.get(key);
    }
    public static void setPayMap(String key,String value) {
        payMap.put(key,value);
    }


}
