package com.ithaorong.reggie.dto;


import com.ithaorong.reggie.entity.OrderDetail;
import com.ithaorong.reggie.entity.Order;
import lombok.Data;
import java.util.List;

@Data
public class OrderDto extends Order {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
