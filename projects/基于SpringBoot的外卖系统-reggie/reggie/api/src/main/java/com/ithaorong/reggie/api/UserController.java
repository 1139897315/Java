package com.ithaorong.reggie.api;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.config.WXAuth;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.service.UserService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供用户相关接口",tags = "用户管理")
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 根据权限获取个人凭证，返回uuid
     * @param code
     * @return
     */
    @GetMapping("/getSessionId")
    @ApiImplicitParam(dataType = "User",name = "user", value = "登录接口",required = true)
    public ResultVO getSessionId(String code){
        return userService.getSessionId(code);
    }

    /**
     * 根据个人凭证到前端
     * @param wxAuth
     * @return
     */
    @PostMapping("/login")
    public ResultVO login(@RequestBody WXAuth wxAuth){
        return userService.authLogin(wxAuth);
    }

    @GetMapping("/getUserByUserId")
    public ResultVO getUserByUserId(Long userId){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,userId)
                    .eq(User::getStatus,0);
        User user = userService.getOne(queryWrapper);
        return ResultVO.success("查询成功！",user);
    }

    @GetMapping("/page")
    public ResultVO list(int page, int pageSize,String name){
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //执行查询，当name不为空
        if(name!=null && name.length() > 0){
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))){
                    queryWrapper.like(User::getName,name);
                }
            }
        }
        //添加排序条件
        queryWrapper.orderByDesc(User::getUpdateTime);

        userService.page(pageInfo,queryWrapper);
        return ResultVO.success("查询成功！", pageInfo);
    }

    @PostMapping("/save")
    public ResultVO save(@RequestBody User user){
//        new SimpleDateFormat("yyyy-MM-dd").parse(user.getBirthday());

        synchronized (this){
            boolean b = userService.updateById(user);
            if (b)
                return ResultVO.success("保存成功！");
            return ResultVO.error("保存失败！");
        }
    }

//
}
