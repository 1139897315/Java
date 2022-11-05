package com.ithaorong.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithaorong.reggie.config.WXAuth;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.vo.ResultVO;

public interface UserService extends IService<User> {
    ResultVO getSessionId(String code);

    ResultVO authLogin(WXAuth wxAuth);
}
