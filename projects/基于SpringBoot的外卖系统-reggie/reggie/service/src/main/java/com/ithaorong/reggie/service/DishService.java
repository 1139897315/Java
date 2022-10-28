package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.vo.ResultVO;


public interface DishService extends IService<Dish> {
    ResultVO saveWithFlavor(DishDto dishDto);
    ResultVO getByIdWithFlavor(Long id);
    ResultVO updateWithFlavor(DishDto dishDto);
}
