package com.ithaorong.reggie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.bytebuddy.asm.Advice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 订单
 */
@Data
@TableName("orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    //订单号
    private String orderId;

    //订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
    private Integer status;

    private Long storeId;

    //下单用户id
    private Long userId;

    //红包金额
    private Integer reduceMoney;

    //购买方式 1外卖 2堂食
    private Integer buyType;

    //性别
    private int sex;

    //店名
    private String storeName;

    //修改用户id
    private Long updateUser;

    //结账时间
    private LocalDateTime checkoutTime;

    //支付方式 1微信，2支付宝

    private Integer payMethod;

    private String untitled;

    private String openId;

    //实收金额
    private BigDecimal amount;

    //自提时间
    private String acquiredTime;

    //备注
    private String remark;

    private String tableware;

    //手机号
    private String phone;

    //地址
    private String address;

    //收货人
    private String consignee;

    //下单时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    private LocalDateTime refundTime;
}
