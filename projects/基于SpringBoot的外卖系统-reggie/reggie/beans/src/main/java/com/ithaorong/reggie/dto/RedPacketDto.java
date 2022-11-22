package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.RedPacket;

import java.time.LocalDateTime;

public class RedPacketDto extends RedPacket {
    private LocalDateTime expireTime;
}
