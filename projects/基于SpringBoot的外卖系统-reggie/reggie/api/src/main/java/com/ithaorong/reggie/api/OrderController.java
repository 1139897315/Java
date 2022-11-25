package com.ithaorong.reggie.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.dto.OrderDto;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.AddressBookService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private AddressBookService addressBookService;

    /**
     * 支付：payMethod（1微信 0支付宝）、amount（实际金额）
     * 备注：remark(非必填)
     * 店名：storeName
     * 地址的各个属性：phone、userName、address、consignee（收货人）
     */
    @PostMapping("/save")
    @Transactional
    public ResultVO save(@RequestHeader String token, @RequestBody Order order){
        if (order == null)
            return ResultVO.error("订单支付数据不能为空,请检查参数再试");
        synchronized (this){
            Map<String,String> map = new HashMap<>();

            String openId;
            try {
                System.out.println("token=========="+token);
                String s = stringRedisTemplate.boundValueOps(token).get();
                System.out.println("s========="+s);
                openId = objectMapper.readValue(s, User.class).getOpenId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }
            //2.保存订单
            order.setId(0L);
            order.setUserId(order.getUserId());
            order.setUpdateUser(order.getUserId());
            order.setOpenId(openId);
            order.setCheckoutTime(null);
            order.setUpdateTime(LocalDateTime.now());
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(1);


            //生成订单编号
            String orderId = order.getUserId() + DateUtil.format(new Date(),"yyyyMMddHHmm")+ RandomUtil.randomNumbers(4);
            order.setOrderId(orderId);

            //2.生成地址快照 -- 前端给

            //3.生成商品快照
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,order.getUserId());
            List<ShoppingCart> list = shoppingCartService.list(queryWrapper);


            StringBuilder untitled = new StringBuilder();
            for (ShoppingCart sc: list) {
                untitled.append(sc.getName()).append(",");

                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(sc,orderDetail);

                orderDetail.setOrderId(orderId);
                orderDetail.setId(0L);

                orderDetailService.save(orderDetail);
            }

            order.setUntitled(String.valueOf(untitled));
            orderService.save(order);

            //5.删除购物车：当购物车中的记录购买成功之后，购物车中对应做删除操作
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShoppingCart::getUserId,order.getUserId())
                            .set(ShoppingCart::getIsDeleted,1);
            boolean is_remove = shoppingCartService.update(updateWrapper);
            //6.增加销量

            if (is_remove)
                return ResultVO.success("保存订单成功！",orderId);
            return ResultVO.error("保存订单失败！");
        }
    }

    /**
     * @param userId 用户id
     * @return 订单列表
     */
    @GetMapping("/listByUserIdOrStatus")
    @ApiOperation("订单查询接口")
    public ResultVO listByUserId(Long userId, Integer status){
        //获取order信息
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userId != 0 ) {
            orderLambdaQueryWrapper.eq(Order::getUserId,userId);
        }
        if (status!=null && (status == 1 || status == 2 ||status == 3 ||status == 4 ||status == 5)) {
            orderLambdaQueryWrapper.eq(Order::getStatus,status);
        }
        List<Order> orderList = orderService.list(orderLambdaQueryWrapper);
        //复制每个order信息

        List<OrderDto> listDto = orderList.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item,orderDto);

            //获取每个order的orderDetail信息
            LambdaQueryWrapper<OrderDetail> orderDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDtoLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderDto.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDtoLambdaQueryWrapper);
            //给OrderDetail赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());
        return ResultVO.success("查询成功！",listDto);
    }

    /**
     * token、订单号和订单状态
     * @param order
     * @return
     */
    @PutMapping("/updateOrderStatus")
    public ResultVO updateOrderStatus(@RequestHeader String token, @RequestBody Order order){
        return orderService.updateOrder(order.getUserId(),order.getOrderId(),order.getStatus());
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
    public ResultVO cancelOrder(@RequestHeader String token, @RequestBody Order order){
        synchronized (this){
            //  1.修改当前订单：status=5 已关闭
            ResultVO resultVO = orderService.updateOrder(order.getUserId(),order.getOrderId(),order.getStatus());
            if (resultVO.getCode() == 1)
                return ResultVO.success("关闭成功！");
            else
                return ResultVO.error("关闭失败！");
        }
    }
}
