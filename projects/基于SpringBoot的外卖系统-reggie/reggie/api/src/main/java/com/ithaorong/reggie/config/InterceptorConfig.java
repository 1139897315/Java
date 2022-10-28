package com.ithaorong.reggie.config;

import com.ithaorong.reggie.interceptor.CheckTokenInterceptor;
import com.ithaorong.reggie.interceptor.SetTimeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;
    @Autowired
    private SetTimeInterceptor setTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkTokenInterceptor)
//                .addPathPatterns("/shopcart/**")
//                .addPathPatterns("/orders/**")
//                .addPathPatterns("/useraddr/**")
//                .addPathPatterns("/user/check")
                .addPathPatterns("/employee/**")
                .excludePathPatterns("/employee/login");


        registry.addInterceptor(setTimeInterceptor).addPathPatterns("/**");

    }
}
