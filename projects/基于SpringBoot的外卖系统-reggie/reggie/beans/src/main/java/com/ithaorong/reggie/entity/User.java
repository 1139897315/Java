package com.ithaorong.reggie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * 用户信息
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    //姓名
    private String name;

    @TableField("nick_name")
    private String nickName;

    private String openId;

    @TableField("union_id")
    private String unionId;

    @TableField("wx_number")
    private String wxNumber;

    private String password;

    //手机号
    private String phone;

    //性别 0 女 1 男
    @TableField("sex")
    private int gender;

    //头像
    @TableField("avatar_url")
    private String avatarUrl;

    //积分
    private int points;

    private int age;

    private Date birthday;

    private double height;

    private String star;

    private String country;

    private String province;

    private String city;

    private String edu;

    private String occu;

    private String selves;

    private String hobbies;

    private String emo;

    private String expections;

    //状态 0:禁用，1:正常
    private int status;

    public void from(UserInfo userInfo){
        openId = userInfo.getOpenId();
        nickName = userInfo.getNickName();
        gender = Integer.parseInt(userInfo.getGender());
        city = userInfo.getCity();
        province = userInfo.getProvince();
        country = userInfo.getCountry();
        avatarUrl = userInfo.getAvatarUrl();
        unionId = userInfo.getUnionId();
    }
}
