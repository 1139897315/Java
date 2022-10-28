package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.entity.Category;
import com.ithaorong.reggie.vo.ResultVO;


public interface CategoryService extends IService<Category> {
    public ResultVO remove(Long id);
}
