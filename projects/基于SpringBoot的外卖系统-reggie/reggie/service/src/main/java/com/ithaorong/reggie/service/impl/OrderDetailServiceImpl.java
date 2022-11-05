package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.OrderDetailMapper;
import com.ithaorong.reggie.entity.OrderDetail;
import com.ithaorong.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
