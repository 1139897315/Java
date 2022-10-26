package com.wxvrv.wxvrv_bar.config;


import com.wxvrv.wxvrv_bar.interceptor.CheckTokenInterceptor;
import com.wxvrv.wxvrv_bar.interceptor.SetTimeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPattern;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;
    @Autowired
    private SetTimeInterceptor setTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkTokenInterceptor)
                .addPathPatterns("/shopcart/**")
                .addPathPatterns("/orders/**")
                .addPathPatterns("/useraddr/**")
                .addPathPatterns("/user/check");
//                .excludePathPatterns();

        registry.addInterceptor(setTimeInterceptor).addPathPatterns("/**");

    }
}
