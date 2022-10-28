package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.SetmealMapper;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.service.SetmealService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.stereotype.Service;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Override
    public ResultVO saveWithFlavor(DishDto dishDto) {
        return null;
    }

    @Override
    public ResultVO getByIdWithFlavor(Long id) {
        return null;
    }

    @Override
    public ResultVO updateWithFlavor(DishDto dishDto) {
        return null;
    }
}
