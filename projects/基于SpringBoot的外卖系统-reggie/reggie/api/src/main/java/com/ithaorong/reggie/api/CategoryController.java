package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.CategoryDto;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.dto.SetmealDto;
import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.DishFlavor;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.service.CategoryService;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.service.SetmealService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供分类相关接口",tags = "分类管理")
@RequestMapping("/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private DishService dishService;
    @Resource
    private SetmealService setmealService;


    /**
     * 添加分类：
     *  post
     *  参数：Category
     *  返回：code = 1
     */
    @PostMapping
    public ResultVO add(@RequestHeader String token, @RequestBody Category category){
        String name = category.getName();

        //根据用户名查询数据库
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name);

        //判断数据是否存在该用户
        synchronized (this){
            Category category_exist = categoryService.getOne(queryWrapper);
            if (category_exist != null)
                return ResultVO.error("该分类已存在");

            //若不存在，则添加用户                （添加用户信息和设置为新用户）
            Long empId;
            try {
                String s = stringRedisTemplate.opsForValue().get(token);
                empId = objectMapper.readValue(s, Employee.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            category.setId(0L);

            category.setCreateTime(LocalDateTime.now());
            category.setUpdateTime(LocalDateTime.now());

            category.setCreateUser(empId);
            category.setUpdateUser(empId);

            categoryService.save(category);
            return ResultVO.success("添加成功！");
        }
    }

    /**
     * 分页查询分类
     *  get
     *  参数：page、pageSize、name
     *  返回：Page
     */
    @GetMapping("/page")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "int",name = "page", value = "当前页",required = true),
            @ApiImplicitParam(dataType = "int",name = "pageSize", value = "每页多少条数据",required = true),
            @ApiImplicitParam(dataType = "String",name = "name", value = "查询的姓名",required = true)
    })
    public ResultVO page(@RequestHeader String token,int page, int pageSize){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<Category>();

        //添加排序条件
        queryWrapper.orderByDesc(Category::getSort);

        categoryService.page(pageInfo,queryWrapper);
        return ResultVO.success("查询成功！", pageInfo);
    }



    @DeleteMapping
    public ResultVO delete(@RequestHeader String token, Long id) {

        return categoryService.remove(id);

    }

    /**
     * 编辑分类：
     *  put
     *  参数：Category
     *  返回：code = 1
     */
    @PutMapping
    @ApiImplicitParam(dataType = "Category",name = "category", value = "分类信息修改接口",required = true)
    public ResultVO update(@RequestHeader String token, @RequestBody Category category){
        synchronized (this) {
            //修改人
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();

            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            category.setUpdateUser(empId);
            //修改时间
            category.setUpdateTime(LocalDateTime.now());
            //根据id修改
            categoryService.updateById(category);
            //返回
            return ResultVO.success("成功修改分类信息！");
        }
    }

    /**
     *根据id获取分类信息接口
     * @param token
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiImplicitParam(dataType = "Long",name = "id", value = "分类信息修改接口",required = true)
    public ResultVO getById(@RequestHeader String token, @PathVariable Long id){

        Category category = categoryService.getById(id);

        if(category != null){
            return ResultVO.success("查询成功！",category);
        }

        return ResultVO.error("没有查询到页面数据");
    }

    /**
     * 根据条件查询分类信息
     *  get
     *  参数：page、pageSize、name
     *  返回：Page
     */
    @GetMapping("/list")
    @ApiImplicitParam(dataType = "Category",name = "category", value = "查询的姓名",required = true)
    public ResultVO list(@RequestHeader String token,Category category){
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);

        return ResultVO.success("查询成功！", list);
    }
    /**
     * 根据条件查询分类以及其下的菜品套餐信息
     */
    @GetMapping("/listAll")
    public ResultVO listAll(){
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getType);
        //获取分类基本信息
        List<Category> list = categoryService.list(queryWrapper);

        //获取菜品、套餐信息
        List<CategoryDto> listDto = list.stream().map((item) -> {
            CategoryDto categoryDto = new CategoryDto();
            BeanUtils.copyProperties(item,categoryDto);

            //菜品分类(包含口味)
            if (categoryDto.getType() == 1){
                Dish dish = new Dish();
                dish.setCategoryId(categoryDto.getId());
                List<DishDto> dishDtos = dishService.list(dish);
                categoryDto.setDishes(dishDtos);
            }
            //套餐分类（包含菜品（包含口味））
            else if (categoryDto.getType() == 2){
                List<SetmealDto> setmealDtos = setmealService.getListByCategoryId(categoryDto.getId());
                categoryDto.setSetmealDtos(setmealDtos);
            }
            //根据分类查找菜品
            return categoryDto;
        }).collect(Collectors.toList());
        System.out.println("listDto===================="+listDto);
        return ResultVO.success("查询成功！", listDto);
    }
}
