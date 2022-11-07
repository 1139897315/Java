package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dao.OrderMapper;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.service.OrderService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ResultVO updateOrderStatus(String token, Order order) {
        synchronized (this){
            Long userId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                userId = objectMapper.readValue(s, User.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getOrderId,order.getOrderId());
            //订单状态、订单id、修改用户、修改时间
            updateWrapper.set(Order::getStatus,order.getStatus())
                    .set(Order::getUpdateTime, LocalDateTime.now())
                    .set(Order::getUpdateUser,userId);

            this.update(updateWrapper);
            return ResultVO.success("修改成功！");
        }
    }
}
