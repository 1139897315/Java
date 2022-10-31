package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dao.SetmealMapper;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.dto.SetmealDto;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.SetmealDishService;
import com.ithaorong.reggie.service.SetmealService;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 添加套餐同时添加套餐菜品关系表
     */
    @Transactional
    public ResultVO saveWithDish(String token , SetmealDto setmealDto) {
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

            setmealDto.setId(0L);
            setmealDto.setCreateTime(LocalDateTime.now());
            setmealDto.setUpdateTime(LocalDateTime.now());

            setmealDto.setCreateUser(empId);
            setmealDto.setUpdateUser(empId);

            //保存套餐基本信息 操作setmeal，执行insert操作
            this.save(setmealDto);

            //保存套餐菜品关系信息 操作setmeal_dish，执行insert操作
            List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
            //由于在SetmealDish中没有setmealid，所有给每个SetmealDish附上setmealid
            //setmeal_dish：类似一个快照表，将菜品id、份数、菜名等都copy一份，并对应套餐id
            //对应的Java实现方式为封装一个List<快照表>

            setmealDishes.stream().map((item)->{
                item.setId(0L);
                item.setSetmealId(setmealDto.getCategoryId());

                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());

                item.setCreateUser(empId);
                item.setUpdateUser(empId);
                return item;
            }).collect(Collectors.toList());

            // for (SetmealDish s : setmealDishes) {
            //     s.setSetmealId(setmealDto.getCategoryId());
            // }

            setmealDishService.saveBatch(setmealDishes);
            return ResultVO.success("添加成功！");
        }
    }

    /**
     * 根据ids批量删除
     * @param ids
     * @return
     */
    @Transactional
    public ResultVO removeByIds(String token, List<Long> ids) {
        synchronized (this){
            //查看套餐状态，确定是否可以删除
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Setmeal::getId,ids);
            queryWrapper.eq(Setmeal::getStatus,1);

            int count = this.count(queryWrapper);
            if (count > 0){
                //如果不能删除，返回失败
                return ResultVO.error("该套餐正在售卖中..删除失败！（请先停止售卖）");
            }

            //如果可以删除，先删除套餐表中数据
            this.removeByIds(ids);

            //再删除关系表中数据
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

            setmealDishService.remove(lambdaQueryWrapper);
            return ResultVO.success("删除成功！");
        }
    }

    public ResultVO getByIdWithDish(Long id) {
        //查询套餐基本信息
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询套餐的口味信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getDishId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);

        return ResultVO.success("查询成功",setmealDto);
    }

    @Transactional
    public ResultVO updateWithDish(String token, SetmealDto setmealDto) {
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

            setmealDto.setUpdateTime(LocalDateTime.now());

            setmealDto.setUpdateUser(empId);

            //更新数据
            this.updateById(setmealDto);

            //删除口味
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId,setmealDto.getId());
            setmealDishService.remove(queryWrapper);

            //添加当前提交过来的口味
            List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
            setmealDishes.stream().map((item) -> {
                item.setDishId(setmealDto.getId());

                item.setUpdateTime(LocalDateTime.now());

                item.setUpdateUser(empId);
                return item;
            }).collect(Collectors.toList());
            setmealDishService.saveBatch(setmealDishes);
        }
        return ResultVO.success("修改成功！");
    }

    @Override
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
            LambdaUpdateWrapper<Setmeal> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.in(Setmeal::getId,ids);
            queryWrapper.set(Setmeal::getUpdateTime,LocalDateTime.now());
            queryWrapper.set(Setmeal::getUpdateUser,empId);
            queryWrapper.set(Setmeal::getStatus,status);

            this.update(queryWrapper);

            return ResultVO.success("修改成功！");
        }
    }

}
