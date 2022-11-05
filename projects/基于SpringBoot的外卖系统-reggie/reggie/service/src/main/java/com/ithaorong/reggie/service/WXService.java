package com.ithaorong.reggie.service;

import com.ithaorong.reggie.entity.UserInfo;

public interface WXService {
    UserInfo wxDecrypt(String encryptedData, String sessionId, String vi) throws Exception;
}
