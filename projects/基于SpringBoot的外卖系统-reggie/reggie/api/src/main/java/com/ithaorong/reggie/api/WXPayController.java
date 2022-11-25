package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.service.OrderService;
import com.ithaorong.reggie.utils.HttpClientUtil;
import com.ithaorong.reggie.utils.WXPayConstants;
import com.ithaorong.reggie.vo.ResultVO;
import com.sun.org.apache.xpath.internal.operations.Or;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ithaorong.reggie.utils.WXPayConstants.SUCCESS;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供微信支付相关接口",tags = "微信支付管理")
@RequestMapping("/wxPay")
public class WXPayController {
    @Resource
    private OrderService orderService;

    @RequestMapping("/prePay")
    public ResultVO prePay(Long id, HttpServletRequest request){

        // 返回参数
        Map<String, Object> resMap = new HashMap<>();
        //获取请求ip地址
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        if(ip.indexOf(",")!=-1){
            String[] ips = ip.split(",");
            ip = ips[0].trim();
        }

        try {
            // 拼接统一下单地址参数
            Map<String, String> paraMap = new HashMap<>();
            Order order =  orderService.getById(id);
            String body = order.getUntitled();//商品名称
            String orderNum = order.getOrderId();//订单号
            String openId = order.getOpenId();
            BigDecimal amount = order.getAmount();
//       Integer price = 1;//支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
            double price = amount.doubleValue() * 100;
            String str_price = String.valueOf(price);
//       body = new String(body.getBytes("ISO-8859-1"),"UTF-8").toString();
//       System.out.println("body= "+body);
            // 封装11个必需的参数
            paraMap.put("appid", WXPayConstants.APP_ID);
            paraMap.put("mch_id", WXPayConstants.MCH_ID);//商家ID
            paraMap.put("nonce_str", WXPayUtil.generateNonceStr());//获取随机字符串 Nonce Str
            paraMap.put("body", body);     //商品名称
            paraMap.put("out_trade_no", orderNum);//订单号
            paraMap.put("fee_type","CNY");
            paraMap.put("total_fee","1");    //测试改为固定金额
            paraMap.put("spbill_create_ip", ip);
            paraMap.put("notify_url",WXPayConstants.CALLBACK_URL);// 此路径是微信服务器调用支付结果通知路径
            paraMap.put("trade_type", "JSAPI");
            paraMap.put("openid", openId);
            String sign = WXPayUtil.generateSignature(paraMap, WXPayConstants.PATERNER_KEY);//商户密码
            //生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
//            paraMap.put("sign_type", "MD5");
            paraMap.put("sign", sign);
            //将所有参数(map)转xml格式
            String xml = WXPayUtil.mapToXml(paraMap);
            System.err.println("xml=: "+xml);
//        xml = new String(xml.getBytes("ISO-8859-1"), "UTF-8");
//       String xml = new String(WXPayUtil.mapToXml(paraMap).getBytes(), "utf-8");
            // 统一下单 https://api.mch.weixin.qq.com/pay/unifiedorder
            String unifiedorder_url = WXPayConstants.UNIFIEDORDER_URL;//统一下单接口
            //发送post请求"统一下单接口"返回预支付id:prepay_id
            String xmlStr = HttpClientUtil.doPostXml(unifiedorder_url, xml);
            System.out.println("xmlStr:"+xmlStr);

            //以下内容是返回前端页面的json数据
            //预支付id
            String prepay_id = "";
            if (xmlStr.contains("SUCCESS")) {
                Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);//XML格式字符串转换为Map
                prepay_id =  map.get("prepay_id").toString();
            }
            Map<String, String > payMap = new HashMap<>();
            // 封装所需6个参数调支付页面
            payMap.put("appId", WXPayConstants.APP_ID);
            payMap.put("timeStamp",System.currentTimeMillis()+"");//获取当前时间戳，单位秒
            payMap.put("nonceStr", WXPayUtil.generateNonceStr());//获取随机字符串 Nonce Str
            payMap.put("signType", "MD5");
            payMap.put("package", "prepay_id="+prepay_id);
            //生成带有 sign 的 XML 格式字符串
            String paySign = WXPayUtil.generateSignature(payMap, WXPayConstants.PATERNER_KEY);
            payMap.put("paySign", paySign);

            // 封装正常情况返回数据
            resMap.put("success","true");
            resMap.put("payMap",payMap);
        } catch (Exception e) {
            // 封装异常情况返回数据
            resMap.put("success","false");
            resMap.put("message","调用统一订单接口错误");
            e.printStackTrace();
        }
        return ResultVO.success("返回成功！",resMap);
    }


    /*支付成功回调*/
    @RequestMapping("/callBack")
    public String callBack(HttpServletRequest request) throws Exception {

        // 1.接收微信支付平台传递的数据（使用request的输入流接收）
        ServletInputStream is = request.getInputStream();
        byte[] bs = new byte[1024];
        int len = -1;
        StringBuilder builder = new StringBuilder();
        while((len = is.read(bs))!=-1){
            builder.append(new String(bs,0,len));
        }
        String s = builder.toString();
        //使用帮助类将xml接口的字符串装换成map
        Map<String, String> map = WXPayUtil.xmlToMap(s);

        //支付成功
        if("success".equalsIgnoreCase(map.get("result_code"))){
            //2.修改订单状态为“待发货/已支付”
            String orderId = map.get("out_trade_no");
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId,orderId);
            Order order = orderService.getOne(queryWrapper);
            //修改状态
            ResultVO resultVO = orderService.updateOrder(order.getUserId(), orderId, 4);
            //修改支付时间
            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getOrderId,orderId)
                        .set(Order::getCheckoutTime, LocalDateTime.now());
            boolean update = orderService.update(updateWrapper);

            //3.通过websocket连接，向前端推送消息
            //WebSocketServer.sendMsg(orderId,"1");

            //4.响应微信支付平台
            if(update){
                HashMap<String,String> resp = new HashMap<>();
                resp.put("return_code","success");
                resp.put("return_msg","OK");
                resp.put("appid",map.get("appid"));
                resp.put("result_code","success");
                return WXPayUtil.mapToXml(resp);
            }
        }
        return null;
    }


//    /*调用退款接口，取消订单*/
    @RequestMapping("/refund")
    @ResponseBody
    public ResultVO refund(Long id, HttpServletResponse response){

        // 返回参数
        Map<String, Object> resMap = new HashMap<>();
        Date newtime = new Date();
        String resXml = "";
        try {
            // 拼接统一下单地址参数
            Map<String, String> paraMap = new HashMap<>();
            Order order = orderService.getById(id);
            String orderId = order.getOrderId();//订单号
            double price = order.getAmount().doubleValue() * 100;
            String str_price = String.valueOf(price);
            System.out.println("str_price============"+str_price);
//       Integer price = 1;//支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
            System.out.println("订单号= "+orderId);
            // 封装必需的参数
            paraMap.put("appid", WXPayConstants.APP_ID);
            paraMap.put("mch_id", WXPayConstants.MCH_ID);//商家ID
            paraMap.put("nonce_str", WXPayUtil.generateNonceStr());//获取随机字符串 Nonce Str
            paraMap.put("out_trade_no", orderId);//订单号
            paraMap.put("out_refund_no", orderId);//商户退款单号
            paraMap.put("total_fee",str_price);    //测试改为固定金额  订单金额
            paraMap.put("refund_fee","1");    //退款金额
            paraMap.put("notify_url", WXPayConstants.notify_url);   //退款路径
            String sign = WXPayUtil.generateSignature(paraMap, WXPayConstants.PATERNER_KEY);//商户密码
            //生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
            paraMap.put("sign", sign);
            //将所有参数(map)转xml格式
            String xml = WXPayUtil.mapToXml(paraMap);
            // 退款 https://api.mch.weixin.qq.com/secapi/pay/refund
            String refund_url = WXPayConstants.REFUND_URL;//申请退款路径接口
            //发送post请求"申请退款"
            String xmlStr = HttpClientUtil.doRefund(refund_url, xml);
            System.out.println("退款xmlStr:"+xmlStr);
            /*退款成功回调修改订单状态*/
            if (xmlStr.indexOf("SUCCESS") != -1) {
                Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);//XML格式字符串转换为Map
                if(map.get("return_code").equals("SUCCESS")){
                    resMap.put("success",true);//此步说明退款成功
                    resMap.put("data","退款成功");
                    System.out.println("退款成功");

                    try {
                        orderService.updateOrder(order.getUserId(),orderId,-1);
                        //告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
                        resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                                + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                        BufferedOutputStream out = new BufferedOutputStream(
                                response.getOutputStream());
                        out.write(resXml.getBytes());
                        out.flush();
                        out.close();
                        System.err.println("返回给微信的值："+resXml.getBytes());
                    }catch (Exception e){
                        resMap.put("fail","订单状态修改失败");
                    }
                }
            }else {
                resMap.put("success","fail");//此步说明退款成功
                resMap.put("data","退款失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultVO.success("退款成功！",resMap);
    }
}
