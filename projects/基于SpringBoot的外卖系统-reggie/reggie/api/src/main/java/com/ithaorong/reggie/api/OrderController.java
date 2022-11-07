package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.OrderDto;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.entity.OrderDetail;
import com.ithaorong.reggie.entity.ShoppingCart;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.service.OrderDetailService;
import com.ithaorong.reggie.service.OrderService;
import com.ithaorong.reggie.service.ShoppingCartService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供订单相关接口",tags = "订单管理")
@RequestMapping("/order")
public class OrderController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderDetailService orderDetailService;



    /**
     * 支付：payMethod（1微信 0支付宝）、amount（实际金额）
     * 备注：remark(非必填)
     * 地址的各个属性：phone、userName、address、consignee（收货人）
     */
    @PostMapping("/save")
    @Transactional
    public ResultVO save(@RequestHeader String token, @RequestBody Order order){
        synchronized (this){

            Map<String,String> map = new HashMap<>();

            Long userId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                userId = objectMapper.readValue(s, User.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }
            //2.保存订单
            order.setId(0L);
            order.setUserId(userId);
            order.setUpdateUser(userId);
            order.setCheckoutTime(null);
            order.setUpdateTime(LocalDateTime.now());
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(1);

            //生成订单编号
            String orderId = UUID.randomUUID().toString().replace("-", "");
            order.setOrderId(orderId);

            //2.生成地址快照 -- 前端给
            orderService.save(order);

            //3.生成商品快照
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,userId);
            List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

            for (ShoppingCart sc: list) {

                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(sc,orderDetail);

                orderDetail.setOrderId(orderId);
                orderDetail.setId(0L);

                orderDetailService.save(orderDetail);
            }

            //5.删除购物车：当购物车中的记录购买成功之后，购物车中对应做删除操作
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShoppingCart::getUserId,userId)
                            .set(ShoppingCart::getIsDeleted,1);
            boolean is_remove = shoppingCartService.update(updateWrapper);

            //发送支付请求
            if (is_remove){
//                String orderId = orderInfo.get("orderId");
//                //设置当前订单信息
//                HashMap<String,String> data = new HashMap<>();
//                data.put("body",orderInfo.get("productNames"));  //商品描述
//                data.put("out_trade_no",orderId);               //使用当前用户订单的编号作为当前支付交易的交易号
//                data.put("fee_type","CNY");                     //支付币种
//                data.put("total_fee",order.getActualAmount()*100+"");          //支付金额
//                data.put("total_fee","1");
//                data.put("trade_type","NATIVE");                //交易类型
//                data.put("notify_url","http://47.118.45.73:8080/pay/callback");           //设置支付完成时的回调方法接口
//


                //微信小程序支付：

//                //发送请求，获取响应
//                //微信支付：申请支付连接
//                WXPay wxPay = new WXPay(new MyPayConfig());
//                Map<String, String> resp = wxPay.unifiedOrder(data);
//                orderInfo.put("payUrl",resp.get("code_url"));
                return ResultVO.success("保存订单成功！");
            }
            return ResultVO.error("保存订单失败！");
        }
    }

    /**
     * @param userId 用户id
     * @param status 订单状态：1待付款，2待派送，3已派送，4已完成，5已取消
     * @return 订单列表
     */
    @GetMapping("/list")
    @ApiOperation("订单查询接口")
    public ResultVO listByUserIdAndStatus(String userId, int status){
        //获取order信息
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.eq(Order::getUserId,userId);
        if (status == 1 || status == 2 || status == 3 || status == 4) {
            orderLambdaQueryWrapper.eq(Order::getStatus,status);
        }
        List<Order> orderList = orderService.list(orderLambdaQueryWrapper);

        //复制每个order信息
        List<OrderDto> list = new ArrayList<>();
        BeanUtils.copyProperties(orderList,list);

        //获取每个order的orderDetail信息
        for (OrderDto orderDto : list) {

            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderDto.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);

            //给OrderDetail赋值
            orderDto.setOrderDetails(orderDetailList);
        }

        return ResultVO.success("查询成功！",list);
    }

    /**
     * token、订单号和订单状态
     * @param order
     * @return
     */
    @PutMapping("/updateOrderStatus")
    public ResultVO updateOrderStatus(@RequestHeader String token, @RequestBody Order order){
        return orderService.updateOrderStatus(token,order);
    }

    /**
     *
     * @param orderId String 订单号
     * @return 订单信息
     */
    @GetMapping("/getOrderByOrderId")
    public ResultVO getOrderById(String orderId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderId,orderId)
                    .in(Order::getStatus,1,2,3,4);
        Order order = orderService.getOne(queryWrapper);

        //复制每个order信息
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(order,orderDto);


        //获取order的orderDetail信息
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderDto.getOrderId());
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);

        //给OrderDetail赋值
        orderDto.setOrderDetails(orderDetailList);

        return ResultVO.success("查询成功！",orderDto);
    }

    @PutMapping("/cancelOrder")
    @Transactional
    public ResultVO cancelOrder(@RequestHeader String token, @RequestParam("orderId") String orderId){
        synchronized (this){
            //  1.修改当前订单：status=5 已关闭
            Order order = new Order();
            order.setOrderId(orderId);
            orderService.updateOrderStatus(token,order);

            return ResultVO.success("关闭成功！");
        }
    }
}
