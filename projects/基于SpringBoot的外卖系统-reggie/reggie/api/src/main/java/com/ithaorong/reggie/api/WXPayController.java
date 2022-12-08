package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.entity.Store;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.service.OrderService;
import com.ithaorong.reggie.service.StoreService;
import com.ithaorong.reggie.service.UserService;
import com.ithaorong.reggie.utils.HttpClientUtil;
import com.ithaorong.reggie.utils.WXPayConstants;
import com.ithaorong.reggie.vo.ResultVO;
import com.sun.org.apache.xpath.internal.operations.Or;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
import java.util.Objects;
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
    @Resource
    private StoreService storeService;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping("/prePay")
    public ResultVO prePay(String orderId, HttpServletRequest request){
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
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId,orderId);
            Order order =  orderService.getOne(queryWrapper);
            String body = order.getUntitled();//商品名称
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
            paraMap.put("out_trade_no", orderId);//订单号
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
                prepay_id =  map.get("prepay_id");
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
        System.out.println("成功进入。。。。。。。。。。。。。。");
        //订单状态改为已完成（4）
        //checkoutTime修改
        //当天客户数+1
        //当天营业额+数额
        //奖励积分

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
            //1.修改状态
            String orderId = map.get("out_trade_no");
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId,orderId);
            Order order = orderService.getOne(queryWrapper);
            ResultVO resultVO = orderService.updateOrder(order.getUserId(), orderId, 4);

            //2.修改支付时间
            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getOrderId,orderId)
                        .set(Order::getCheckoutTime, LocalDateTime.now());
            boolean update = orderService.update(updateWrapper);

            //3.当天客户数+1
            //4.当天营业额+数额
            BigDecimal amount = order.getAmount();
            double price = amount.doubleValue();
            String str_price = String.valueOf(price);

            LambdaUpdateWrapper<Store> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(Store::getId,order.getStoreId())
                        .setSql("day_customers = day_customers + 1")
                        .setSql("day_turnover = day_turnover + "+str_price);
            storeService.update(updateWrapper1);

            //5.增加积分
            LambdaUpdateWrapper<User> updateWrapper2 = new LambdaUpdateWrapper<>();
            if (price <= 10){
                updateWrapper2.eq(User::getId,order.getUserId())
                        .set(User::getPoints,10);
            }else if (price > 10 && price <= 50){

            }else
            userService.update(updateWrapper2);

            //发送RabbitMQ支付成功，等操作(解耦操作)

            //6.响应微信支付平台
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
    @Transactional
    public ResultVO refund(@RequestHeader String token, String orderId, HttpServletResponse response){
        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        Long storeId = employee.getStoreId();
        int ranking = employee.getRanking();

        // 返回参数
        Map<String, Object> resMap = new HashMap<>();
        Date newtime = new Date();
        String resXml = "";
        try {
            // 拼接统一下单地址参数
            Map<String, String> paraMap = new HashMap<>();
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrderId,orderId);
            Order order = orderService.getOne(queryWrapper);
            if (( (ranking == 1 || ranking == 2) && Objects.equals(order.getStoreId(), storeId)) || ranking == 3 ){

                double price = order.getAmount().doubleValue() * 100;
                String str_price = String.valueOf(price);
//       Integer price = 1;//支付金额，单位：分，这边需要转成字符串类型，否则后面的签名会失败
                System.out.println("订单号= "+orderId);
                // 封装必需的参数
                paraMap.put("appid", WXPayConstants.APP_ID);
                paraMap.put("mch_id", WXPayConstants.MCH_ID);//商家ID
                paraMap.put("nonce_str", WXPayUtil.generateNonceStr());//获取随机字符串 Nonce Str
                paraMap.put("out_trade_no", orderId);//订单号
                paraMap.put("out_refund_no", orderId);//商户退款单号
                paraMap.put("total_fee","1");    //测试改为固定金额  订单金额
                paraMap.put("refund_fee","1");    //退款金额
//            paraMap.put("notify_url", WXPayConstants.notify_url);   //退款路径
                String sign = WXPayUtil.generateSignature(paraMap, WXPayConstants.PATERNER_KEY);//商户密码
                //生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
                paraMap.put("sign", sign);
//            paraMap.put("sign_type", "MD5");
                //将所有参数(map)转xml格式
                String xml = WXPayUtil.mapToXml(paraMap);
                // 退款 https://api.mch.weixin.qq.com/secapi/pay/refund
                String refund_url = WXPayConstants.REFUND_URL;//申请退款路径接口
                //发送post请求"申请退款"
                System.out.println("========================================");
                System.out.println("WXPayConstants.APP_ID"+ WXPayConstants.APP_ID);
                System.out.println("WXPayConstants.MCH_ID"+ WXPayConstants.MCH_ID);
                System.out.println("nonce_str===="+ WXPayUtil.generateNonceStr());
                System.out.println("out_trade_no===="+ orderId);
                System.out.println("out_refund_no===="+ orderId);
                System.out.println("total_fee===="+ "1");
                System.out.println("notify_url===="+ WXPayConstants.notify_url);
                System.out.println("sign===="+ sign);
                System.out.println("========================================");
                String xmlStr = HttpClientUtil.doRefund(refund_url, xml);
                System.out.println("退款xmlStr:"+xmlStr);
                /*退款成功回调修改订单状态*/

                if (xmlStr.contains("SUCCESS")) {
                    Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);//XML格式字符串转换为Map
                    if(map.get("return_code").equals("SUCCESS")){
                        resMap.put("success",true);//此步说明退款成功
                        resMap.put("data","退款成功");
                        System.out.println("退款成功");

                        //修改订单信息
                        Order update_order = new Order();
                        order.setId(order.getId());
                        order.setRefundTime(LocalDateTime.now());
                        order.setUpdateTime(LocalDateTime.now());
                        order.setStatus(6);
                        orderService.updateById(order);

                        //给微信支付平台成功反馈（暂时有问题）
//                        try {
//                            orderService.updateOrder(order.getUserId(),String.valueOf(orderId),-1);
//                            //告诉微信服务器收到信息了，不要在调用回调action了========这里很重要回复微信服务器信息用流发送一个xml即可
//                            resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
//                                    + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
//                            BufferedOutputStream out = new BufferedOutputStream(
//                                    response.getOutputStream());
//                            out.write(resXml.getBytes());
//                            out.flush();
//                            out.close();
//                            System.err.println("返回给微信的值："+resXml.getBytes());
//                        }catch (Exception e){
//                            resMap.put("fail","订单状态修改失败");
//                        }
                    }
                }else {
                    resMap.put("success","fail");//此步说明退款成功
                    resMap.put("data","退款失败");
                    return ResultVO.error("退款失败！",resMap);
                }
            //判断是否本店或权限为3的订单
            }else{
                return ResultVO.error("该订单非本门店或当前权限不足！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("resMap=========="+resMap);
        return ResultVO.success("退款成功！",resMap);
    }
}
