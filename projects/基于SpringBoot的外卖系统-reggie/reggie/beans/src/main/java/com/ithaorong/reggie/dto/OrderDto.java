package com.ithaorong.reggie.dto;


import com.ithaorong.reggie.entity.OrderDetail;
import com.ithaorong.reggie.entity.Order;
import lombok.Data;
import java.util.List;

@Data
public class OrderDto extends Order {

    private List<OrderDetail> orderDetails;
	
}
