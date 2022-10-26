package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.service.EmployeeService;
import com.ithaorong.reggie.vo.ResultVO;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
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
        System.out.println("在这..");
        //生成token
        JwtBuilder builder = Jwts.builder();
        // HashMap<String,Object> map = new HashMap<>();
        // map.put("key1","value1");
        // map.put("key2","value2");

        String token = builder.setSubject(username)                 //主题，就是token中携带的数据
                .setIssuedAt(new Date())                            //设置token的生成时间
                .setId(username + "")                               //设置用户id为token  id
                // .setClaims(map)                                     //map中可以存放用户的角色权限信息
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

    @PostMapping
    public ResultVO add(@RequestHeader String token, @RequestBody Employee new_emp){
        String username = new_emp.getUsername();
        String password = new_emp.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,username);
        Employee is_emp = employeeService.getOne(queryWrapper);

        //判断数据是否存在该用户
        synchronized (this){
            if (is_emp != null)
                return ResultVO.error("该用户已存在");

            //若不存在，则添加用户                （添加用户信息和设置为新用户）
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            new_emp.setPassword(password);

            new_emp.setCreateTime(LocalDateTime.now());
            new_emp.setUpdateTime(LocalDateTime.now());

            new_emp.setCreateUser(empId);
            new_emp.setUpdateUser(empId);

            log.info(new_emp.toString());
            employeeService.save(new_emp);
            return ResultVO.success("添加成功！");
        }
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public ResultVO page(int page, int pageSize,String name){
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
        //执行查询，当name不为空
        if(name!=null && name.length() > 0){
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))){
                    queryWrapper.like(Employee::getName,name);
                }

            }
        }
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,queryWrapper);
        return ResultVO.success("", pageInfo);
    }

//    @GetMapping
//    public ResultVO query(@RequestHeader String token){
//        Employee employee;
//        try {
//            String s = stringRedisTemplate.boundValueOps(token).get();
//            employee = objectMapper.readValue(s, Employee.class);
//        } catch (JsonProcessingException e) {
//            return ResultVO.error("出现异常！");
//        }
//        return ResultVO.success("获取成功！",employee);
//    }



}

