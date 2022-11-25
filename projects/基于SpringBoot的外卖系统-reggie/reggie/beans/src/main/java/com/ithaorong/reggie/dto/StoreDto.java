package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.Desk;
import com.ithaorong.reggie.entity.Store;
import lombok.Data;

import java.util.List;

@Data
public class StoreDto extends Store {
    private List<Desk> desks;

}
