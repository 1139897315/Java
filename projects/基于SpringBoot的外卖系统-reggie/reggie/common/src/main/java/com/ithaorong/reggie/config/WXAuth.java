package com.ithaorong.reggie.config;

import lombok.Data;

@Data
public class WXAuth {
    private String encryptedData;//用户敏感信息
    private String iv;//解密算法的向量
    private String sessionId;//返回的uuid，获取
}
