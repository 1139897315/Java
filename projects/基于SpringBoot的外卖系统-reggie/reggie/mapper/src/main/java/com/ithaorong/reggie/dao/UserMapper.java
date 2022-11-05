package com.ithaorong.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ithaorong.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
