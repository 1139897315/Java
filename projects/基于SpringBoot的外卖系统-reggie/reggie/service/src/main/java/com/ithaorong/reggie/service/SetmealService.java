package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.dto.SetmealDto;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.vo.ResultVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    ResultVO saveWithDish(String token, SetmealDto setmealDto);
    ResultVO removeByIds(String token, List<Long> ids);
    ResultVO getByIdWithDish(Long id);
    ResultVO updateWithDish(String token, SetmealDto setmealDto);
    List<SetmealDto> getListByCategoryId(Long storeId,Long categoryId);
    ResultVO updateStatusById(String token, int status, List<Long> ids);
}
