package com.wxvrv.wxvrv_bar.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxvrv.wxvrv_bar.beans.entity.Employee;
import com.wxvrv.wxvrv_bar.common.vo.ResultVO;

public interface EmployeeService extends IService<Employee> {
    public ResultVO checkLogin();
}
