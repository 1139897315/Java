package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.CategoryMapper;
import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.service.CategoryService;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.service.SetmealService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public ResultVO remove(Long id) {
        synchronized (this) {
            //select count(*) from dish where categoryId = ?
            //要查询Dish
            LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
            //添加查询条件
            dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
            int count1 = dishService.count(dishLambdaQueryWrapper);
            //如果关联了菜品，则抛出业务异常
            if (count1 > 0) {
                return ResultVO.error("当前分类关联了菜品！不能删除");
            }

            //要查询Setmeal
            LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();
            //添加查询条件
            setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
            //如果关联了套餐，则抛出业务异常
            int count2 = setmealService.count(setmealLambdaQueryWrapper);
            if (count2 > 0) {
                return ResultVO.error("当前分类关联了套餐！不能删除");
            }

            //正常，则删除分类
            super.removeById(id);
            return ResultVO.success("成功删除分类！");
        }
    }
}
