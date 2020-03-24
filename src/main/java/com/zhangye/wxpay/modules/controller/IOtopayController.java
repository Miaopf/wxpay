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

@Controller
public class IOtopayController {

    @Autowired
    private WxMenuService wxMenuService;

    /**
     * 二维码首页 测试用
     */
    @RequestMapping(value = {"/iotopay"}, method = RequestMethod.GET)
    public String wxPayList(Model model) {
        //商户订单号
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
                       @RequestParam(value = "totalFee") int totalFee,@RequestParam(value = "channelid") String channelid) throws Exception {
        //模拟测试订单信息
        Order order = new Order();
        order.setClintIp("123.12.12.123");
        order.setSubject("ESM365充值卡");
        order.setTotalFee(totalFee);
        //获取二维码链接
        String codeUrl = wxMenuService.iotpay(order,channelid,signType);
        if (!StringUtils.isNotBlank(codeUrl)) {
            System.out.println("----生成二维码失败----");
            
        } else {
            //根据链接生成二维码
            WxUtil.writerPayImage(response, codeUrl);
        }
    }
}
