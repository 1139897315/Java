package com.ithaorong.reggie.service.impl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ithaorong.reggie.config.RedisKey;
import com.ithaorong.reggie.entity.UserInfo;
import com.ithaorong.reggie.service.WXService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;


@Component
public class WXServiceImpl implements WXService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

//    public UserInfo getUserInfo(String encryptedData, String sessionId, String iv) throws Exception {
//        System.out.println("===========encryptedData:"+encryptedData);
//        System.out.println("===========sessionId:"+sessionId);
//        System.out.println("===========iv:"+iv);
//        String json = stringRedisTemplate.opsForValue().get(RedisKey.WX_SESSION_ID + sessionId);
//        System.out.println("===========json:"+json);
//        JSONObject jsonObject = JSON.parseObject(json);
//        String sessionKey = (String) jsonObject.get("session_key");
//        // 被加密的数据
//        byte[] dataByte = Base64.decode(encryptedData);
//        // 加密秘钥
//        byte[] keyByte = Base64.decode(sessionKey);
//        // 偏移量
//        byte[] ivByte = Base64.decode(iv);
//
//        // 如果密钥不足16位，那么就补足. 这个if 中的内容很重要
//        int base = 16;
//        if (keyByte.length % base != 0) {
//            int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
//            byte[] temp = new byte[groups * base];
//            Arrays.fill(temp, (byte) 0);
//            System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
//            keyByte = temp;
//        }
//        // 初始化
//        Security.addProvider(new BouncyCastleProvider());
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
//        SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
//        AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
//        parameters.init(new IvParameterSpec(ivByte));
//        cipher.init( Cipher.DECRYPT_MODE, spec, parameters);// 初始化
//        byte[] resultByte = cipher.doFinal(dataByte);
//        if (null != resultByte && resultByte.length > 0) {
//            String result = new String(resultByte, "UTF-8");
//            return JSON.parseObject(result, UserInfo.class);
//        }
//        return null;
//    }

    public UserInfo wxDecrypt(String encryptedData, String sessionId, String vi) throws Exception{
        //开始解密 -- res（session_key。。。）
        String json = stringRedisTemplate.opsForValue().get(sessionId);
//        JSONObject jsonObject = objectMapper.readValue(json, JSONObject.class);
        JSONObject jsonObject = JSON.parseObject(json);
        String sessionKey = (String) jsonObject.get("session_key");
        byte[] encData = Base64.decode(encryptedData);
        byte[] iv = Base64.decode(vi);
        byte[] key = Base64.decode(sessionKey);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE,keySpec,ivSpec);
        return JSON.parseObject(new String(cipher.doFinal(encData),"UTF-8"), UserInfo.class);
    }
}
