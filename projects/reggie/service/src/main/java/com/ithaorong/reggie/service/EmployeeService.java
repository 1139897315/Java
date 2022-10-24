package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.vo.ResultVO;

public interface EmployeeService extends IService<Employee> {
    public ResultVO checkLogin();
}
