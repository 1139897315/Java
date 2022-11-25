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

    public ResultVO updateOrder(Long userId, String orderId,int status) {
        synchronized (this){
            try {
                LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Order::getOrderId,orderId);
                //订单状态、订单id、修改用户、修改时间
                if (status != 0)
                    updateWrapper.set(Order::getStatus,status);

                updateWrapper.set(Order::getUpdateTime, LocalDateTime.now())
                        .set(Order::getUpdateUser,userId);

                this.update(updateWrapper);
            }catch (Exception e){
                return ResultVO.error("出现异常！");
            }
            return ResultVO.success("修改成功！");
        }
    }

//    @Override
//    public ResultVO updateOrder(Order order) {
//        synchronized (this){
//            this.updateById(order);
//        }
//        return null;
//    }
}
