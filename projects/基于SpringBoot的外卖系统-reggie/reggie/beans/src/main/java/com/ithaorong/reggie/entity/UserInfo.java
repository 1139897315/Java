package com.ithaorong.reggie.entity;

import lombok.Data;

import java.util.HashMap;

@Data
public class UserInfo {
    /**
     * 微信可获取：
     * openId、nickName、gender、city、province、country、avatarUrl、unionId、watermark{appid、timestamp}
     */
    private String openId;
    private String nickName;
    private String gender;
    private String city;
    private String province;
    private String country;
    private String language;
    private String avatarUrl;
    private String unionId;
    private HashMap<String ,String> watermark;
}
