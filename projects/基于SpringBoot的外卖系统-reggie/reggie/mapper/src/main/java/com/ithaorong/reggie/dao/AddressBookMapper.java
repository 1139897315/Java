package com.ithaorong.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ithaorong.reggie.entity.AddressBook;
import com.ithaorong.reggie.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
