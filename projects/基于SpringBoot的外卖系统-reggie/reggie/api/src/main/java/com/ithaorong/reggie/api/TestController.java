package com.ithaorong.reggie.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.vo.ResultVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class TestController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping("/send")
    public String sendMsg(){
        System.out.println("123456");
        return "123456";
    }
    @PostMapping("/accept")
    public ResultVO sendMsg(@RequestHeader String token,@RequestBody HashMap<String ,String >map){
        String username = map.get("username");
        String password = map.get("password");
        System.out.println("token=============="+token);
        System.out.println("username=============="+username);
        System.out.println("password=============="+password);
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("user",username);
        map1.put("pwd",password);
        return ResultVO.success("接受成功！",map1);
    }
}
