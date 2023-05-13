package com.shier.partner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        /**
         * 设置允许跨域的路径
         * 所有请求都允许跨域，使用这种配置就不需要
         * 在interceptor中配置header了
         */
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //是否允许证书 不再默认开启
                //当Credentials为true时，Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowCredentials(true)
                /* 放自己的前端域名*/
                .allowedOriginPatterns("http://partner.kongshier.top, http://127.0.0.1:5173, http://localhost:5173")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                //设置允许的方法
                .allowedMethods("*")
                .allowedHeaders("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
