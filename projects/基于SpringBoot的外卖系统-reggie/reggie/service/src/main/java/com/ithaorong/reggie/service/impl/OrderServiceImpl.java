package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.OrderMapper;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
