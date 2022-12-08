package com.ithaorong.reggie.dto;

import com.ithaorong.reggie.entity.RedPacket;
import lombok.Data;
import net.bytebuddy.asm.Advice;

import java.time.LocalDateTime;

@Data
public class RedPacketDto extends RedPacket {
    //剩下多少时间过期
    private LocalDateTime expireTime;
}
