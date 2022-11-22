package com.ithaorong.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Store implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private String name;
    private int status;
    private int isDelete;
    private String detailAddress;
    private int ranking;
    private Long dayCustomers;
    private Long monthCustomers;
    private Long yearCustomers;
    private Long dayTurnover;
    private Long monthTurnover;
    private Long yearTurnover;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;


}
