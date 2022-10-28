package com.ithaorong.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ithaorong.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//BaseMapper<Employee>：常见的增删改查方法都继承
public interface EmployeeMapper extends BaseMapper<Employee> {
}
