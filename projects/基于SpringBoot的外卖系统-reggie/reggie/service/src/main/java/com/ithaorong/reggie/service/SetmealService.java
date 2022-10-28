package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.vo.ResultVO;

public interface SetmealService extends IService<Setmeal> {
    ResultVO saveWithFlavor(DishDto dishDto);
    ResultVO getByIdWithFlavor(Long id);
    ResultVO updateWithFlavor(DishDto dishDto);
}
