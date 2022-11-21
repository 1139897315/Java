package com.ithaorong.reggie.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.config.WXAuth;
import com.ithaorong.reggie.service.UserService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

//    @GetMapping("/getUser")
//    public ResultVO getUserByOpenId(){
//        return
//    }

//    /**
//     * 添加员工
//     * @param token
//     * @param employee
//     * @return
//     */
//    @PostMapping
//    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "添加员工接口",required = true)
//    public ResultVO add(@RequestHeader String token, @RequestBody Employee employee){
//        String username = employee.getUsername();
//
//        String password = employee.getPassword();
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
//
//        //根据用户名查询数据库
//        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Employee::getUsername, username);
//        //判断数据是否存在该用户
//        synchronized (this){
//            Employee emp = employeeService.getOne(queryWrapper);
//            if (emp != null)
//                return ResultVO.error("该用户已存在");
//
//            //若不存在，则添加用户                （添加用户信息和设置为新用户）
//            Long empId;
//            try {
//                String s = stringRedisTemplate.opsForValue().get(token);
//                empId = objectMapper.readValue(s, Employee.class).getId();
//            } catch (JsonProcessingException e) {
//                return ResultVO.error("出现异常！");
//            }
//
//            employee.setId(0L);
//            employee.setPassword(password);
//
//            employee.setCreateTime(LocalDateTime.now());
//            employee.setUpdateTime(LocalDateTime.now());
//
//            employee.setCreateUser(empId);
//            employee.setUpdateUser(empId);
//
//            employeeService.save(employee);
//            return ResultVO.success("添加成功！");
//        }
//    }
//
//    /**
//     * 员工信息分页查询
//     * @param page
//     * @param pageSize
//     * @param name
//     * @return
//     */
//    @GetMapping("/page")
//    @ApiImplicitParams({
//            @ApiImplicitParam(dataType = "int",name = "page", value = "当前页",required = true),
//            @ApiImplicitParam(dataType = "int",name = "pageSize", value = "每页多少条数据",required = true),
//            @ApiImplicitParam(dataType = "String",name = "name", value = "查询的姓名",required = true)
//    })
//    public ResultVO page(@RequestHeader String token,int page, int pageSize,String name){
//        //构造分页构造器
//        Page pageInfo = new Page(page,pageSize);
//
//        //构造条件构造器
//        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
//        //执行查询，当name不为空
//        if(name!=null && name.length() > 0){
//            for (int i = 0; i < name.length(); i++) {
//                if (!Character.isWhitespace(name.charAt(i))){
//                    queryWrapper.like(Employee::getName,name);
//                }
//
//            }
//        }
//        //添加排序条件
//        queryWrapper.orderByDesc(Employee::getUpdateTime);
//
//        employeeService.page(pageInfo,queryWrapper);
//        return ResultVO.success("查询成功！", pageInfo);
//    }
//
//    /**
//     * 修改员工信息接口
//     * @param token
//     * @param employee
//     * @return
//     */
//    @PutMapping
//    @ApiImplicitParam(dataType = "Employee",name = "employee", value = "员工信息修改接口",required = true)
//    public ResultVO update(@RequestHeader String token, @RequestBody Employee employee){
//        synchronized (this) {
//            //修改人
//            Long empId;
//            try {
//                String s = stringRedisTemplate.boundValueOps(token).get();
//                empId = objectMapper.readValue(s, Employee.class).getId();
//
//            } catch (JsonProcessingException e) {
//                return ResultVO.error("出现异常！");
//            }
//
//            employee.setUpdateUser(empId);
//            //修改时间
//            employee.setUpdateTime(LocalDateTime.now());
//            //根据id修改
//            employeeService.updateById(employee);
//            //返回
//            return ResultVO.success("成功修改员工信息！");
//        }
//    }
//
//    /**
//     *根据id获取员工信息接口
//     * @param token
//     * @param id
//     * @return
//     */
//    @GetMapping("/{id}")
//    @ApiImplicitParam(dataType = "Long",name = "id", value = "员工信息修改接口",required = true)
//    public ResultVO getById(@RequestHeader String token, @PathVariable Long id){
//
//        Employee employee = employeeService.getById(id);
//
//        if(employee != null){
//            return ResultVO.success("查询成功！",employee);
//        }
//
//        return ResultVO.error("没有查询到页面数据");
//    }
}
