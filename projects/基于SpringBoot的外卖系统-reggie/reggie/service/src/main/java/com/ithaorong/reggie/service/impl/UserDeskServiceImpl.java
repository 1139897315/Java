package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.UserDeskMapper;
import com.ithaorong.reggie.entity.UserDesk;
import com.ithaorong.reggie.service.UserDeskService;
import org.springframework.stereotype.Service;

@Service
public class UserDeskServiceImpl extends ServiceImpl<UserDeskMapper, UserDesk> implements UserDeskService {
}
