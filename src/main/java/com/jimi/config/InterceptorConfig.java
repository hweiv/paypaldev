package com.jimi.config;

import com.jimi.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册TestInterceptor拦截器
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")  //添加需要拦截的路径
                .excludePathPatterns("/**/login", "/**/logout", "/**/bank/*", "/**/dingding/*")				//添加不拦截的请求路径
                ;
    }

    @Bean
    public LoginInterceptor loginInterceptor(){
        return new LoginInterceptor();
    }
}
