package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dao.DishMapper;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.DishFlavorService;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Resource
    private DishFlavorService dishFlavorService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResultVO saveWithFlavor(String token, DishDto dishDto) {
        synchronized (this){
            //获取用户id
            //修改人
            Long empId;
            Long storeId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();
                storeId = objectMapper.readValue(s, Employee.class).getStoreId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            dishDto.setId(0L);
            dishDto.setStoreId(storeId);
            dishDto.setCreateTime(LocalDateTime.now());
            dishDto.setUpdateTime(LocalDateTime.now());

            dishDto.setCreateUser(empId);
            dishDto.setUpdateUser(empId);
            //保存菜品数据
            this.save(dishDto);

            Long dishId = dishDto.getId();
            //保存菜品口味数据
            List<DishFlavor> flavors = dishDto.getFlavors();
            flavors.stream().map((item) -> {
                item.setId(0L);
                item.setDishId(dishId);

                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());

                item.setCreateUser(empId);
                item.setUpdateUser(empId);
                return item;
            }).collect(Collectors.toList());



            dishFlavorService.saveBatch(flavors);


            return ResultVO.success("添加成功！");
        }
    }

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

        return ResultVO.success("查询成功",dishDto);
    }

    @Transactional
    public ResultVO updateWithFlavor(String token, DishDto dishDto) {
        synchronized (this){
            //获取用户id
            //修改人
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            dishDto.setUpdateTime(LocalDateTime.now());

            dishDto.setUpdateUser(empId);

            //更新数据
            this.updateById(dishDto);

            //删除口味
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            dishFlavorService.remove(queryWrapper);

            //添加当前提交过来的口味
            List<DishFlavor> flavors = dishDto.getFlavors();
            flavors.stream().map((item) -> {
                item.setId(0L);
                item.setDishId(dishDto.getId());


                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());

                item.setCreateUser(empId);
                item.setUpdateUser(empId);
                return item;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);
        }
        return ResultVO.success("修改成功！");
    }

    @Transactional
    public ResultVO delete(String token, List<Long> ids) {
        synchronized (this){
            //查看套餐状态，确定是否可以删除
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Dish::getId,ids);
            queryWrapper.eq(Dish::getStatus,1);

            int count = this.count(queryWrapper);
            if (count > 0){
                //如果不能删除，返回失败
                return ResultVO.error("该菜品正在售卖中..删除失败！（请先停止售卖）");
            }

            //如果可以删除，先删除套餐表中数据
            this.removeByIds(ids);

            //再删除关系表中数据
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(DishFlavor::getDishId,ids);

            dishFlavorService.remove(lambdaQueryWrapper);
            return ResultVO.success("删除成功！");
        }
    }

    @Override
    public List<DishDto> list(Dish dish) {

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件，查询状态为1的（起售）
        queryWrapper.eq(Dish::getStatus,1);
        //获取该分类下的
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId())
                    .eq(Dish::getStoreId,dish.getStoreId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = this.list(queryWrapper);

        List<DishDto> listDto = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //根据菜品查找口味
            LambdaUpdateWrapper<DishFlavor> dishFlavorLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            dishFlavorLambdaUpdateWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaUpdateWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        return listDto;
    }

    public ResultVO updateStatusById(String token, int status, List<Long> ids) {
        synchronized (this){
            //获取用户id
            //修改人
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }
            //条件
            LambdaUpdateWrapper<Dish> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.in(Dish::getId,ids);
            queryWrapper.set(Dish::getUpdateTime,LocalDateTime.now());
            queryWrapper.set(Dish::getUpdateUser,empId);
            queryWrapper.set(Dish::getStatus,status);

            this.update(queryWrapper);

            return ResultVO.success("修改成功！");
        }
    }
}
