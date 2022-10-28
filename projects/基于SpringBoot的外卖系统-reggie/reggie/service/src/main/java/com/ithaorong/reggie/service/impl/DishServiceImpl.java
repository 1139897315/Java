package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.DishMapper;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.DishFlavor;
import com.ithaorong.reggie.service.DishFlavorService;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Resource
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public ResultVO saveWithFlavor(DishDto dishDto) {
        synchronized (this){
            //保存菜品数据
            this.save(dishDto);

            //保存菜品口味数据
            Long dishId = dishDto.getId();
            List<DishFlavor> flavors = dishDto.getFlavors();
            flavors.stream().map((item) -> {
                item.setDishId(dishId);
                return item;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);

            return ResultVO.success("添加成功！");
        }
    }

    @Override
    public ResultVO getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(list);

        return ResultVO.success("查询成功");
    }

    @Override
    @Transactional
    public ResultVO updateWithFlavor(DishDto dishDto) {
        synchronized (this){
            //更新数据
            this.updateById(dishDto);

            //删除口味
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            dishFlavorService.remove(queryWrapper);

            //添加当前提交过来的口味
            List<DishFlavor> flavors = dishDto.getFlavors();
            flavors.stream().map((item) -> {
                item.setDishId(dishDto.getId());
                return item;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);
        }
        return ResultVO.success("修改成功！");
    }
}
