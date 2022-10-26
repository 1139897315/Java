package com.wxvrv.wxvrv_bar.interceptor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class SetTimeInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if(token != null){
            String s = stringRedisTemplate.boundValueOps(token).get();
            if(s != null){
                stringRedisTemplate.boundValueOps(token).expire(30, TimeUnit.MINUTES);
            }
        }
        return true;
    }
}
