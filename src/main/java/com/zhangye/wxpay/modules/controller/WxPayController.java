package com.zhangye.wxpay.modules.controller;

import com.alibaba.fastjson.JSONObject;
import com.zhangye.wxpay.modules.common.wx.WxConfig;
import com.zhangye.wxpay.modules.common.wx.WxConstants;
import com.zhangye.wxpay.modules.common.wx.WxUtil;
import com.zhangye.wxpay.modules.model.Order;
import com.zhangye.wxpay.modules.service.WxMenuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;

/**
 * @author zhangye
 * @version 1.0
 * @description 微信扫码支付接口
 * @date 2019/12/19
 * <p>
 * 微信支付接口官方文档地址:https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_5
 * 本 demo 使用的支付方式为: 模式二
 * <p>
 * 微信扫码支付流程说明:
 * 1.需要商户生成订单
 * 2.商户调用微信统一下单接口获取二维码链接 code_url (请求参数请见官方文档)
 * 请求参数中的 notify_url 为用户支付成功后, 微信服务端回调商户的接口地址
 * 3.商户根据 code_url 生成二维码
 * 4.用户使用微信扫码进行支付
 * 5.支付成功后, 微信服务端会调用 notify_url 通知商户支付结果
 * 6.商户接到通知后, 执行业务操作(修改订单状态等)并告知微信服务端接收通知成功
 * <p>
 * 查询微信支付订单、关闭微信支付订单流程较为简单，请自行查阅官方文档
 */
@Controller
public class WxPayController {

    @Autowired
    private WxMenuService wxMenuService;

    /**
     * 二维码首页 测试用
     */
    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String wxPayList(Model model) {
        //商户订单号
        model.addAttribute("outTradeNo", WxUtil.mchOrderNo());
        return "/wxPayList";
    }

    /**
     * 获取订单流水号 测试用
     */
    @RequestMapping(value = {"/wxPay/outTradeNo"})
    @ResponseBody
    public String getOutTradeNo(Model model) {
        //商户订单号
        return WxUtil.mchOrderNo();
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
    @RequestMapping(value = {"/wxPay/payUrl"})
    public void payUrl(HttpServletRequest request, HttpServletResponse response,
                       @RequestParam(value = "totalFee") int totalFee,
                       @RequestParam(value = "outTradeNo") String outTradeNo,
                       @RequestParam(value = "productId") String productId) throws Exception {
        //模拟测试订单信息
        Order order = new Order();
        order.setClintIp("123.12.12.123");
        order.setOrderNo(outTradeNo);
        order.setProductId(productId);
        order.setSubject("ESM365充值卡");
        order.setTotalFee(totalFee);
        //获取二维码链接
        String codeUrl = wxMenuService.wxPayUrl(order, signType);
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
    @RequestMapping(value = {"/wxPay/unifiedorderNotify"})
    public void unifiedorderNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //商户订单号
        String outTradeNo = null;
        String xmlContent = "<xml>" +
                "<return_code><![CDATA[FAIL]]></return_code>" +
                "<return_msg><![CDATA[签名失败]]></return_msg>" +
                "</xml>";

        try {
            String requestXml = WxUtil.getStreamString(request.getInputStream());
            System.out.println("requestXml : " + requestXml);
            Map<String, String> map = WxUtil.xmlToMap(requestXml);
            String returnCode = map.get(WxConstants.RETURN_CODE);
            //校验一下 ，判断是否已经支付成功
            if (StringUtils.isNotBlank(returnCode) && StringUtils.equals(returnCode, "SUCCESS") && WxUtil.isSignatureValid(map, WxConfig.key, signType)) {
                //商户订单号
                outTradeNo = map.get("out_trade_no");
                System.out.println("outTradeNo : " + outTradeNo);
                //微信支付订单号
                String transactionId = map.get("transaction_id");
                System.out.println("transactionId : " + transactionId);
                //支付完成时间
                SimpleDateFormat payFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date payDate = payFormat.parse(map.get("time_end"));

                SimpleDateFormat systemFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("支付时间：" + systemFormat.format(payDate));
                //临时缓存
                WxConfig.setPayMap(outTradeNo, "SUCCESS");

                //根据支付结果修改数据库订单状态
                //其他操作
                //......

                //给微信的应答 xml, 通过 response 回写
                xmlContent = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "</xml>";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WxUtil.responsePrint(response, xmlContent);
    }

    /**
     * 前台页面定时器查询是否已支付
     * 1.前台页面轮询
     * 2.查询订单支付状态
     */
    @RequestMapping(value = {"/wxPay/payStatus"})
    @ResponseBody
    public String payStatus(@RequestParam(value = "outTradeNo") String outTradeNo) {
        JSONObject responseObject = new JSONObject();
        //从临时缓存中取
        String outTradeNoValue = WxConfig.getPayMap(outTradeNo);
        String status = "200";
        //判断是否已经支付成功
        if (StringUtils.isNotBlank(outTradeNoValue)) {
            if (StringUtils.equals(outTradeNoValue, "SUCCESS")) {
                status = "0";
            } else if (StringUtils.equals(outTradeNoValue, "CODE_URL_ERROR")) {
                //生成二维码失败
                status = "1";
            }
        } else {
            //如果临时缓存中没有 去数据库读取
            //......
        }
        responseObject.put("status", status);
        return responseObject.toJSONString();
    }

    /**
     * 微信支付订单查询
     * 1.如果由于网络通信问题 导致微信没有通知到商户支付结果
     * 2.商户主动去查询支付结果 而后执行其他业务操作
     */
    @RequestMapping(value = {"/wxPay/orderQuery"})
    @ResponseBody
    public String orderQuery(@RequestParam(value = "orderNo") String orderNo) throws Exception {
        String result = wxMenuService.wxOrderQuery(orderNo, signType);
        return result;
    }

    /**
     * 关闭微信支付订单
     * 1.商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付
     * 2.系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口
     */
    @RequestMapping(value = {"/wxPay/closeOrder"})
    @ResponseBody
    public String closeOrder(@RequestParam(value = "orderNo") String orderNo) throws Exception {
        String result = wxMenuService.wxCloseOrder(orderNo, signType);
        return result;
    }

    //申请退款
    //查询退款
}
