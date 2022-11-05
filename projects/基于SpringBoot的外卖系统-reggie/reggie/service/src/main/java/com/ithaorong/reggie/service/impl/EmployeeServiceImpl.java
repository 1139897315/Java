package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.EmployeeMapper;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.service.EmployeeService;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{

    @Resource
    private EmployeeMapper employeeMapper;

    public ResultVO checkLogin(){

        return new ResultVO();
    }
}
