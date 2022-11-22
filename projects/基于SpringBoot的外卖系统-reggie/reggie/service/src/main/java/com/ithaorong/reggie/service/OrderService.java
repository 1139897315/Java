package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.vo.ResultVO;

public interface OrderService extends IService<Order> {
    ResultVO updateOrderStatus(Long userId, String orderId, int status);
}
