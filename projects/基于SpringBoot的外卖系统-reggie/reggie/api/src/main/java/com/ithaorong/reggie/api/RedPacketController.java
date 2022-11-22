package com.ithaorong.reggie.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供红包相关接口",tags = "红包管理")
@RequestMapping("/redPacket")
public class RedPacketController {

}
