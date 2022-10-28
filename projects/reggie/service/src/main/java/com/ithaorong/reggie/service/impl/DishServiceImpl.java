package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.DishMapper;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
