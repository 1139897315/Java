package com.wxvrv.wxvrv_bar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.wxvrv.wxvrv_bar.beans.entity.Employee;
import com.wxvrv.wxvrv_bar.common.vo.ResultVO;
import com.wxvrv.wxvrv_bar.service.EmployeeService;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供员工相关接口",tags = "员工管理")
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/login")
    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "输入账号、密码",required = true)
    public ResultVO login(@RequestBody Employee employee){
        String username = employee.getUsername();
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,username);
        Employee emp = employeeService.getOne(queryWrapper);
        //判断数据是否存在该用户
        if (emp == null)
            return ResultVO.error("该用户不存在");
        //判断该用户密码是否正确
        if (!emp.getPassword().equals(password))
            return ResultVO.error("密码错误");
        //判断员工状态是否可用
        if (!(emp.getStatus() == 1))
            return ResultVO.error("该账号禁用");
        //生成token
        JwtBuilder builder = Jwts.builder();
        // HashMap<String,Object> map = new HashMap<>();
        // map.put("key1","value1");
        // map.put("key2","value2");

        String token = builder.setSubject(username)                     //主题，就是token中携带的数据
                .setIssuedAt(new Date())                                //设置token的生成时间
                .setId(username + "")                                   //设置用户id为token  id
                // .setClaims(map)                                      //map中可以存放用户的角色权限信息
                .setExpiration(new Date(System.currentTimeMillis() + 24*60*60*1000)) //设置token过期时间
                .signWith(SignatureAlgorithm.HS256, "ithaorong")     //设置加密方式和加密密码
                .compact();
        //当用户登录成功之后，将token存入redis
        try {
            String userInfo = objectMapper.writeValueAsString(emp);
            stringRedisTemplate.boundValueOps(token).set(userInfo, 30, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        return ResultVO.success("登录成功！",token);
    }


    @PostMapping("/loginout")
    @ApiImplicitParam(dataType = "String",name = "token", value = "输入token",required = true)
    public ResultVO loginout(@RequestHeader String token){
        stringRedisTemplate.delete(token);
        return ResultVO.success("");
    }

//    @PostMapping("/register")
//    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "输入账号、密码、昵称、头像等员工的信息",required = true)
//    public ResultVO register(@RequestBody Employee employee){
//        //根据账号判断是否已存在
//
//        //若存在，则返回一个失败信息
//
//        //若不存在，则添加用户信息和设置为新用户
//        return ResultVO.success("");
//    }

}
