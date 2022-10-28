package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
