package com.zhangye.wxpay.modules.controller;

import com.alibaba.fastjson.JSONObject;
import com.zhangye.wxpay.modules.common.wx.IotopayConfig;
import com.zhangye.wxpay.modules.common.wx.WxConfig;
import com.zhangye.wxpay.modules.common.wx.WxConstants;
import com.zhangye.wxpay.modules.common.wx.WxUtil;
import com.zhangye.wxpay.modules.model.Order;
import com.zhangye.wxpay.modules.service.WxMenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IOtopayController {

    @Autowired
    @Qualifier("IotpayMenuService")
    private WxMenuService wxMenuService;

    /**
     * 二维码首页 测试用
     */
    @RequestMapping(value = {"/iotopay"}, method = RequestMethod.GET)
    public String wxPayList(Model model) {
        //商户订单号
    	model.addAttribute("outTradeNo", WxUtil.mchOrderNo());
        return "/iotopay";
    }
    /**
     * 默认 signType 为 md5
     */
    final private String signType = WxConstants.SING_MD5;
    /**
     * 微信支付统一下单-生成二维码
     * 1.请求微信预下单接口
     * 2.根据预下单返回的 code_url 生成二维码
     * 3.将二维码 write 到前台页面
     */
    @RequestMapping(value = {"/iotopay/createorder"})
    public void payUrl(HttpServletRequest request, HttpServletResponse response,
                       @RequestParam(value = "totalFee") int totalFee,
                       @RequestParam(value = "outTradeNo") String outTradeNo,
                       @RequestParam(value = "channelid") String channelid) throws Exception {
        //模拟测试订单信息
        Order order = new Order();
        order.setClintIp("123.12.12.123");
        order.setOrderNo(outTradeNo);
        order.setSubject("ESM365");
        order.setTotalFee(totalFee);
        //获取二维码链接
        String codeUrl = wxMenuService.iotpay(order,channelid,signType);
        if (!StringUtils.isNotBlank(codeUrl)) {
            System.out.println("----生成二维码失败----");
            WxConfig.setPayMap(outTradeNo, "CODE_URL_ERROR");
        } else {
            //根据链接生成二维码
            WxUtil.writerPayImage(response, codeUrl);
        }
    }
    
    /**
     * 微信支付统一下单-通知链接
     * 1.用户支付成功后
     * 2.微信回调该方法
     * 3.商户最终通知微信已经收到结果
     */
    @RequestMapping(value = {"/iotopay/unifiedorderNotify"})
    public void unifiedorderNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //商户订单号
        String outTradeNo = null;
        String xmlContent = "faild";

        try {
			Map<String, String> map = new HashMap();
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = (String) paramNames.nextElement();
				String paramValue = request.getParameter(paramName);
				System.out.println(paramName + "" + paramValue);
				map.put(paramName, paramValue);
			}
			
			System.out.println(map);
            String returnCode = map.get("status");
            //校验一下 ，判断是否已经支付成功
            if (StringUtils.isNotBlank(returnCode) && StringUtils.equals(returnCode, "2") && WxUtil.isSignatureValid(map, IotopayConfig.mchKey, signType)) {
                //商户订单号
                outTradeNo = map.get("mchOrderNo");
                System.out.println("mchOrderNo : " + outTradeNo);
                //微信支付订单号
                String transactionId = map.get("payOrderId");
                System.out.println("payOrderId : " + transactionId);
                //支付完成时间
                SimpleDateFormat payFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date payDate = payFormat.parse(map.get("paySuccTime"));

                SimpleDateFormat systemFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("支付时间：" + systemFormat.format(payDate));
                //临时缓存
                WxConfig.setPayMap(outTradeNo, "SUCCESS");

                //根据支付结果修改数据库订单状态
                //其他操作
                //......

                //给微信的应答 xml, 通过 response 回写
                xmlContent = "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WxUtil.responsePrint(response, xmlContent);
    }
    
    /**
     * 获取订单流水号 测试用
     */
    @RequestMapping(value = {"/iotopay/outTradeNo"})
    @ResponseBody
    public String getOutTradeNo(Model model) {
        //商户订单号
        return WxUtil.mchOrderNo();
    }
    
    @RequestMapping(value = {"/iotopay/orderQuery"})
    @ResponseBody
    public String orderQuery(@RequestParam(value = "orderNo") String orderNo) throws Exception {
        String result = wxMenuService.wxOrderQuery(orderNo, signType);
        return result;
    }
}
