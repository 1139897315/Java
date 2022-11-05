package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;

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
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/login")
    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "登录接口",required = true)
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

    @PostMapping("/loginOut")
    public ResultVO loginOut(@RequestHeader String token){
        synchronized (this){
            stringRedisTemplate.delete(token);
            return ResultVO.success("登录成功！",token);
        }
    }

    /**
     * 添加员工
     * @param token
     * @param employee
     * @return
     */
    @PostMapping
    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "添加员工接口",required = true)
    public ResultVO add(@RequestHeader String token, @RequestBody Employee employee){
        String username = employee.getUsername();

        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        //判断数据是否存在该用户
        synchronized (this){
            Employee emp = employeeService.getOne(queryWrapper);
            if (emp != null)
                return ResultVO.error("该用户已存在");

            //若不存在，则添加用户                （添加用户信息和设置为新用户）
            Long empId;
            try {
                String s = stringRedisTemplate.opsForValue().get(token);
                empId = objectMapper.readValue(s, Employee.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            employee.setId(0L);
            employee.setPassword(password);

            employee.setCreateTime(LocalDateTime.now());
            employee.setUpdateTime(LocalDateTime.now());

            employee.setCreateUser(empId);
            employee.setUpdateUser(empId);

            employeeService.save(employee);
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
    @ApiImplicitParams({
        @ApiImplicitParam(dataType = "int",name = "page", value = "当前页",required = true),
        @ApiImplicitParam(dataType = "int",name = "pageSize", value = "每页多少条数据",required = true),
        @ApiImplicitParam(dataType = "String",name = "name", value = "查询的姓名",required = true)
    })
    public ResultVO page(@RequestHeader String token,int page, int pageSize,String name){
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
        return ResultVO.success("查询成功！", pageInfo);
    }

    /**
     * 修改员工信息接口
     * @param token
     * @param employee
     * @return
     */
    @PutMapping
    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "员工信息修改接口",required = true)
    public ResultVO update(@RequestHeader String token, @RequestBody Employee employee){
        synchronized (this) {
            //修改人
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();

            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            employee.setUpdateUser(empId);
            //修改时间
            employee.setUpdateTime(LocalDateTime.now());
            //根据id修改
            employeeService.updateById(employee);
            //返回
            return ResultVO.success("成功修改员工信息！");
        }
    }

    /**
     * 传入id和密码
     * @param token
     * @param employee
     * @return
     */
    @PutMapping("/updatePassword")
    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "员工密码修改接口",required = true)
    public ResultVO updatePassword(@RequestHeader String token, @RequestBody Employee employee){
        synchronized (this) {
            //修改人
            Long empId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                empId = objectMapper.readValue(s, Employee.class).getId();

            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }
            LambdaUpdateWrapper<Employee> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Employee::getId,employee.getId());
            updateWrapper.set(Employee::getPassword,DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));
            updateWrapper.set(Employee::getUpdateTime,LocalDateTime.now());
            updateWrapper.set(Employee::getUpdateUser,empId);
            //根据id修改
            employeeService.update(updateWrapper);
            //返回
            return ResultVO.success("成功修改员工密码！");
        }
    }
    /**
     *根据id获取员工信息接口
     * @param token
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiImplicitParam(dataType = "Long",name = "id", value = "员工信息修改接口",required = true)
    public ResultVO getById(@RequestHeader String token, @PathVariable Long id){

        Employee employee = employeeService.getById(id);
        if(employee != null){
            return ResultVO.success("查询成功！",employee);
        }

        return ResultVO.error("没有查询到页面数据");
    }


}

