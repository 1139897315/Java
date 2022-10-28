package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
