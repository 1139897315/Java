package com.wxvrv.wxvrv_bar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.wxvrv.wxvrv_bar.beans.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
//BaseMapper<Employee>：常见的增删改查方法都继承
public interface EmployeeMapper extends BaseMapper<Employee> {
}
