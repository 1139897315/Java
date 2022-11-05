package com.ithaorong.reggie.config;

import com.ithaorong.reggie.interceptor.CheckTokenInterceptor;
import com.ithaorong.reggie.interceptor.SetTimeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;
    @Autowired
    private SetTimeInterceptor setTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkTokenInterceptor)
                .addPathPatterns("/employee/**")
                .excludePathPatterns("/employee/login")
                .addPathPatterns("/dish/**")
                .excludePathPatterns("/dish/listAll")
                .addPathPatterns("/category/**")
                .excludePathPatterns("/category/listAll")
                .addPathPatterns("/setmeal/**");


        registry.addInterceptor(setTimeInterceptor).addPathPatterns("/**");

    }
}
