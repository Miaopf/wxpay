package com.zhangye.wxpay.modules.service.impl;

import com.zhangye.wxpay.modules.common.http.HttpsClient;
import com.zhangye.wxpay.modules.common.wx.IoPayConstants;
import com.zhangye.wxpay.modules.common.wx.IotopayConfig;
import com.zhangye.wxpay.modules.common.wx.WxConfig;
import com.zhangye.wxpay.modules.common.wx.WxConstants;
import com.zhangye.wxpay.modules.common.wx.WxUtil;
import com.zhangye.wxpay.modules.model.Order;
import com.zhangye.wxpay.modules.service.WxMenuService;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangye
 * @version 1.0
 * @description 微信支付实现类
 * @date 2019/12/19
 */
@Service("IotpayMenuService")
public class IotpayMenuServiceImpl implements WxMenuService {

    @Override
    public String wxPayUrl(Order order, String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //公众账号ID
        data.put("appid", WxConfig.appID);
        //商户号
        data.put("mch_id", WxConfig.mchID);
        //随机字符串
        data.put("nonce_str", WxUtil.getNonceStr());
        //商品描述
        data.put("body", order.getSubject());
        //商户订单号
        data.put("out_trade_no", order.getOrderNo());
        //标价币种
        data.put("fee_type", "CNY");
        //标价金额
        data.put("total_fee", String.valueOf(order.getTotalFee()));
        //用户的IP
        data.put("spbill_create_ip", order.getClintIp());
        //通知地址
        data.put("notify_url", WxConfig.unifiedorderNotifyUrl);
        //交易类型
        data.put("trade_type", "NATIVE");
        //签名类型
        data.put("sign_type", signType);
        //商品id
        data.put("product_id", order.getProductId());
        //签名 签名中加入key
        data.put("sign", WxUtil.getSignature(data, WxConfig.key, signType));

        String requestXML = WxUtil.mapToXml(data);
        String responseString = HttpsClient.httpsRequestReturnString(WxConstants.PAY_UNIFIEDORDER, HttpsClient.METHOD_POST, requestXML);
        //解析返回的xml
        Map<String, String> resultMap = WxUtil.processResponseXml(responseString, signType);
        if (resultMap.get(WxConstants.RETURN_CODE).equals("SUCCESS")) {
            return resultMap.get("code_url");
        }
        return null;
    }

    @Override
    public String wxOrderQuery(String orderNo, String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //商户号
        data.put("mchId", IotopayConfig.mchId);
        //随机字符串
        data.put("payOrderId", "");
        //商户订单号
        data.put("mchOrderNo", orderNo);
        //签名类型
        data.put("sign_type", signType);
        //签名 签名中加入key
        data.put("sign", WxUtil.getSignature(data, IotopayConfig.mchKey, signType));
        JSONObject json = new JSONObject(data);
        String responseString = HttpsClient.httpPost(IoPayConstants.PAY_ORDERQUERY, json.toString());
        System.out.println(responseString);
        JSONObject d = new JSONObject(responseString);
		if (d.get(IoPayConstants.RETURN_CODE).equals("SUCCESS")) {
			JSONObject result = d.getJSONObject("result");
		    return result.get("status") + "";
		}
        return null;
    }

    @Override
    public String wxCloseOrder(String orderNo, String signType) throws Exception {
        HashMap<String, String> data = new HashMap<String, String>();
        //公众账号ID
        data.put("appid", WxConfig.appID);
        //商户号
        data.put("mch_id", WxConfig.mchID);
        //随机字符串
        data.put("nonce_str", WxUtil.getNonceStr());
        //商户订单号
        data.put("out_trade_no", orderNo);
        //签名类型
        data.put("sign_type", signType);
        //签名 签名中加入key
        data.put("sign", WxUtil.getSignature(data, WxConfig.key, signType));
        String requestXML = WxUtil.mapToXml(data);
        String responseString = HttpsClient.httpsRequestReturnString(WxConstants.PAY_CLOSEORDER, HttpsClient.METHOD_POST, requestXML);
        //解析返回的xml
        Map<String, String> resultMap = WxUtil.processResponseXml(responseString, signType);
        if (resultMap.get(WxConstants.RETURN_CODE).equals("SUCCESS")) {
            /**
             * 关闭订单状态
             * SUCCESS—关闭成功
             * FAIL—关闭失败
             */
            return resultMap.get("result_code");
        }
        return null;
    }

	@Override
	public String iotpay(Order order, String channelid, String signType) throws Exception {
		try {
			String order_sn =  WxUtil.getNonceStr();
			String sceneInfo = "";
			if(channelid != null && "WX_NATIVE".equals(channelid)) {
				sceneInfo = "{\"productId\":\""+order_sn+"\"}";
			}
			HashMap<String, String> data = new HashMap<String, String>();
			//商户号
			data.put("mchId", IotopayConfig.mchId);
			data.put("mchOrderNo", order.getOrderNo());
			data.put("extra", sceneInfo);
			data.put("channelId", channelid);
			data.put("currency", "CAD");
			data.put("amount", String.valueOf(order.getTotalFee()));
			data.put("clientIp", order.getClintIp());
			data.put("device", "WEB");
			data.put("notifyUrl", IotopayConfig.unifiedorderNotifyUrl);
			data.put("subject", "subject"+order.getSubject());
			data.put("body", "body"+order.getSubject());
     //签名 签名中加入key
			data.put("sign", WxUtil.getSignature(data, IotopayConfig.mchKey, signType));

			System.out.println(data);
			JSONObject json = new JSONObject(data);
			String param = "param="+json.toString();
			System.out.println(param);
			String responseString = HttpsClient.httpPost(IoPayConstants.PAY_UNIFIEDORDER, json.toString());
			System.out.println(responseString);
			//解析返回的xml
			JSONObject result = new JSONObject(responseString);
			if (result.get(IoPayConstants.RETURN_CODE).equals("SUCCESS") || result.get(IoPayConstants.RES_CODE).equals("SUCCESS")) {
				// 区分微信和支付宝
				switch(channelid){
					case "ALIPAY_QR":
						return (String) result.get("qr_code");
					case "WX_NATIVE":
						return (String) result.get("codeUrl");
				}
			}
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
