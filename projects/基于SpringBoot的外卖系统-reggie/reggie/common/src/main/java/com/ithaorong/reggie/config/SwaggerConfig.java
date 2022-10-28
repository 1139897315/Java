package com.ithaorong.reggie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket getDocket(){


        //指定生成文档中的封面信息：文档标题、版本、作者..
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title("《瑞吉外卖》后端接口说明")
                .description("该文档详细说明了瑞吉外卖的后端接口规范")
                .version("v 1.0.0")
                .contact(new Contact("陈浩荣","","1139897315@qq.com"));
        ApiInfo apiInfo = new ApiInfoBuilder().build();

        //指定文档风格
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ithaorong.reggie.api"))
                // .paths(PathSelectors.regex("/user")) //只为user路径下生成文档
                .paths(PathSelectors.any())             //任何路径
                .build();
        return docket;
    }
}
