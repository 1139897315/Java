package com.ithaorong.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class RedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Integer reduceMoney;

    private Integer fullMoney;
}
