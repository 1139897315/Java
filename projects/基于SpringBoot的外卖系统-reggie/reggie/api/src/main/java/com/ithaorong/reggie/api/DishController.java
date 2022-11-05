package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.DishDto;
import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.service.CategoryService;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供菜品相关接口",tags = "菜品管理")
@RequestMapping("/dish")
public class DishController {

    @Resource
    private DishService dishService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;


    /**
     * 新增菜品（较难）
     *    1.查询当前的所有分类（前端去调用）
     *    2.页面请求进行图片上传，将图片保存在服务器上
     *    3.将页面下载，回显到浏览器中
     *    4.点击保存按钮，发送异步请求，将菜品提交给服务端
     */
    @PostMapping
    public ResultVO save(@RequestHeader String token, @RequestBody DishDto dishDto) {

        return dishService.saveWithFlavor(token,dishDto);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiImplicitParam(dataType = "Dish",name = "dish", value = "菜品信息修改接口",required = true)
    public ResultVO update(@RequestHeader String token, @RequestBody DishDto dishDto){
        return dishService.updateWithFlavor(token, dishDto);
    }

    /**
     *根据id获取Dish信息接口
     * @param token
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiImplicitParam(dataType = "Long",name = "id", value = "菜品信息修改接口",required = true)
    public ResultVO getById(@RequestHeader String token, @PathVariable Long id){

        return dishService.getByIdWithFlavor(id);
    }

    @PostMapping("/status/{status}")
    public ResultVO updateStatusById(@RequestHeader String token, @PathVariable int status, @RequestParam("ids") List<Long> ids){
        return dishService.updateStatusById(token, status, ids);
    }


    /**
     * 分页查询菜品
     */
    @GetMapping("/page")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "int",name = "page", value = "当前页",required = true),
            @ApiImplicitParam(dataType = "int",name = "pageSize", value = "每页多少条数据",required = true),
            @ApiImplicitParam(dataType = "String",name = "name", value = "查询的姓名",required = true)
    })
    public ResultVO page(@RequestHeader String token,int page, int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //执行查询，当name不为空
        if(name!=null && name.length() > 0){
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))){
                    queryWrapper.like(Dish::getName,name);
                }

            }
        }

        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return ResultVO.success("查询成功！", pageInfo);
    }

    /**
     * 根据条件查询菜品信息（排查）
     */
    @GetMapping("/list")
    public ResultVO list(@RequestHeader String token, Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件，查询状态为1的（起售）
        queryWrapper.eq(Dish::getStatus,1);
        //获取该分类下的
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return ResultVO.success("查询成功！",list);
    }
    /**
     * 根据条件查询菜品信息
     */
    @GetMapping("/listAll")
    public ResultVO listAll(){
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>();
        //添加条件，查询状态为1的（起售）
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getCategoryId);

        List<Dish> list = dishService.list(queryWrapper);

        return ResultVO.success("查询成功！", list);
    }

    /**
     * 删除菜品
     */
    @DeleteMapping
    public ResultVO delete(@RequestHeader String token, @RequestParam("ids") List<Long> ids){
        return dishService.delete(token, ids);
    }
}
