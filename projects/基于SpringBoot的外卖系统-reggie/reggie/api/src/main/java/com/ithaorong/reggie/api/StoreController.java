package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.StoreDto;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.DeskService;
import com.ithaorong.reggie.service.StoreService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.xml.transform.Result;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供门店相关接口",tags = "门店管理")
@RequestMapping("/store")
public class StoreController {
    @Resource
    private StoreService storeService;
    @Resource
    private DeskService deskService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/save")
    @Transactional
    public ResultVO save(@RequestHeader String token, @RequestBody StoreDto storeDto){
        Employee emp;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            emp = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        Long storeId = emp.getStoreId();
        int ranking = emp.getRanking();
        if(ranking == 3){
            synchronized (this){
                storeDto.setId(0L);
                storeDto.setStatus(0);
                storeDto.setCreateTime(LocalDateTime.now());
                storeDto.setUpdateTime(LocalDateTime.now());
                storeDto.setIsDelete(0);
                storeDto.setRanking(0);
                storeDto.setDayCustomers(0L);
                storeDto.setMonthCustomers(0L);
                storeDto.setYearCustomers(0L);
                storeDto.setDayTurnover(0L);
                storeDto.setMonthTurnover(0L);
                storeDto.setYearTurnover(0L);
                storeDto.setName(storeDto.getName());
                storeDto.setDetailAddress(storeDto.getDetailAddress());
                boolean is_OK = storeService.save(storeDto);

                //保存套餐菜品关系信息 操作setmeal_dish，执行insert操作
                List<Desk> desks = storeDto.getDesks();
                //由于在SetmealDish中没有setmealid，所有给每个SetmealDish附上setmealid
                //setmeal_dish：类似一个快照表，将菜品id、份数、菜名等都copy一份，并对应套餐id
                //对应的Java实现方式为封装一个List<快照表>

                // for (SetmealDish s : setmealDishes) {
                //     s.setSetmealId(setmealDto.getCategoryId());
                // }
                if (is_OK)
                    return ResultVO.success("保存成功！");
                return ResultVO.error("保存出错！");
            }
        }
        return ResultVO.error("请检查您的等级！");
    }

    @GetMapping("/page")
    public ResultVO page(@RequestHeader String token,int page, int pageSize,String name){

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();

        //执行查询，当name不为空
        if(name!=null && name.length() > 0){
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))){
                    queryWrapper.like(Store::getName,name);
                }
            }
        }
        //添加排序条件
        queryWrapper.orderByDesc(Store::getUpdateTime)
                    .eq(Store::getIsDelete,0);

        storeService.page(pageInfo,queryWrapper);
        return ResultVO.success("查询成功！", pageInfo);
    }

    /**
     *
     * @param store id必传，另外需要修改哪些字段就传哪些字段
     * @return
     */
    @PutMapping("/update")
    public ResultVO update(@RequestHeader String token, @RequestBody Store store){
        Employee emp;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            emp = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        Long storeId = emp.getStoreId();
        int ranking = emp.getRanking();
        if (ranking == 2 || ranking == 1){
            //修改自己店面的信息
            Long update_storeId = store.getId();
            if (Objects.equals(storeId, update_storeId)){
                synchronized (this){
                    store.setUpdateTime(LocalDateTime.now());
                    storeService.updateById(store);
                    return ResultVO.success("修改成功！");
                }
            }else {
                return ResultVO.error("只能修改自己门店信息");
            }
        }else if (ranking == 3){
            synchronized (this){
                store.setUpdateTime(LocalDateTime.now());
                storeService.updateById(store);
                return ResultVO.success("修改成功！");
            }
        }else{
            return ResultVO.error("店员和店长只能修改本门店信息，店长以上可修改其他门店信息");
        }
    }
    @DeleteMapping("/delete")
    public ResultVO delete(@RequestHeader String token, Long id){
        Employee emp;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            emp = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = emp.getRanking();
        if (ranking == 3){
            synchronized (this){
                LambdaUpdateWrapper<Store> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Store::getId,id)
                        .set(Store::getIsDelete,1);
                storeService.update(updateWrapper);
                return ResultVO.success("删除成功！");
            }
        }else{
            return ResultVO.error("店长以上可以删除！");
        }
    }
    @GetMapping("/getStoreById")
    public ResultVO getStoreById(@RequestHeader String token, Long id){
        LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Store::getId,id)
                .eq(Store::getIsDelete,0);
        Store store = storeService.getOne(queryWrapper);

        StoreDto storeDto = new StoreDto();
        BeanUtils.copyProperties(store,storeDto);

        return ResultVO.success("查询成功！",store);
    }

    @GetMapping("/list")
    public ResultVO list(){
        LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Store::getIsDelete,0);
        List<Store> list = storeService.list(queryWrapper);
        return ResultVO.success("查询成功！",list);
    }
}
