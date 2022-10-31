package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.vo.ResultVO;

import java.util.List;


public interface DishService extends IService<Dish> {
    ResultVO saveWithFlavor(String token, DishDto dishDto);
    ResultVO getByIdWithFlavor(Long id);
    ResultVO updateWithFlavor(String token, DishDto dishDto);
    ResultVO delete(String token, List<Long> ids);

    ResultVO updateStatusById(String token, int status, List<Long> ids);
}
