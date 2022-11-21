package com.ithaorong.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithaorong.reggie.dao.DeskMapper;
import com.ithaorong.reggie.entity.Desk;
import com.ithaorong.reggie.service.DeskService;
import org.springframework.stereotype.Service;

@Service
public class DeskServiceImpl extends ServiceImpl<DeskMapper, Desk> implements DeskService {
}
