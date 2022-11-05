package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.dto.SetmealDto;
import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.service.CategoryService;
import com.ithaorong.reggie.service.SetmealService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供套餐相关接口",tags = "套餐管理")
@RequestMapping("/setmeal")
public class SetmealController {
    @Resource
    private SetmealService setmealService;
    @Resource
    private CategoryService categoryService;

    @PostMapping
    @ApiImplicitParam(dataType = "SetmealDto",name = "setmealDto", value = "添加套餐接口",required = true)
    public ResultVO save(@RequestHeader String token, @RequestBody SetmealDto setmealDto){
        return setmealService.saveWithDish(token,setmealDto);
    }

    /**
     * 根据条件查询套餐信息
     */
    @GetMapping("/listAll")
    public ResultVO listAll(){
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件，查询状态为1的（起售）
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getCategoryId).orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return ResultVO.success("查询成功！", list);
    }

    @GetMapping("/page")
    public ResultVO page(@RequestHeader String token, int page, int pageSize,String name){

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);


        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) ->{
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item,setmealDto);

            Long categoryId = item.getCategoryId();

            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return ResultVO.success("查询成功！",dtoPage);
    }

    @DeleteMapping
    public ResultVO delete(@RequestHeader String token,@RequestParam("ids") List<Long> ids){
        return setmealService.removeByIds(token, ids);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiImplicitParam(dataType = "SetmealDto",name = "setmealDto", value = "套餐信息修改接口",required = true)
    public ResultVO update(@RequestHeader String token, @RequestBody SetmealDto setmealDto){
        return setmealService.updateWithDish(token, setmealDto);
    }

    /**
     *根据id获取Dish信息接口
     * @param token
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiImplicitParam(dataType = "Long",name = "id", value = "根据id查询套餐信息接口",required = true)
    public ResultVO getById(@RequestHeader String token, @PathVariable Long id){
        return setmealService.getByIdWithDish(id);
    }

    @PostMapping("/status/{status}")
    public ResultVO updateStatusById(@RequestHeader String token, @PathVariable int status, @RequestParam("ids") List<Long> ids){
        return setmealService.updateStatusById(token, status, ids);
    }
}
