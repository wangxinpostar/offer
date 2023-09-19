package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    String[] path = {
            "/user/code",
            "/user/login",
            "/upload/**",
            "/blog/hot",
            "/shop/**",
            "/shop-type/**",
            "/voucher/**"};
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(path).order(1);

        registry.addInterceptor(new RefreshTokenInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //允许跨域访问资源定义
        registry.addMapping("/**")
                //(只允许本地的指定端口访问)允许所有
                .allowedOrigins("http://localhost:8080")
                // 允许发送凭证: 前端如果配置改属性为true之后，则必须同步配置
                .allowCredentials(true)
                // 允许所有方法
                .allowedMethods("*")

                .allowedHeaders("*");
    }

}
