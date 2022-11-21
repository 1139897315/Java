package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.entity.Dish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDto extends Category {
    private List<DishDto> dishes = new ArrayList<>();
    private List<SetmealDto> setmealDtos = new ArrayList<>();
}
