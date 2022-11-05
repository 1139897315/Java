package com.ithaorong.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ithaorong.reggie.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
