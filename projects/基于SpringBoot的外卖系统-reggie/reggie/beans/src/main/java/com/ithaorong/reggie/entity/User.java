package com.ithaorong.reggie.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.format.annotation.DateTimeFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private double height;

    private String star;

    private String city;

    private String edu;

    private String occu;

    private String selves;

    private String hobbies;

    private String expections;

    //状态 0:正常，1:禁用
    private int status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public void from(UserInfo userInfo){
        openId = userInfo.getOpenId();
        nickName = userInfo.getNickName();
        gender = Integer.parseInt(userInfo.getGender());
        city = userInfo.getCity();
        avatarUrl = userInfo.getAvatarUrl();
        unionId = userInfo.getUnionId();
    }
}
