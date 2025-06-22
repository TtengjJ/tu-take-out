package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login", "/user/user/sendMsg", "/user/user/register");
    }

    /**
     * 配置 OpenAPI (Knife4j)
     */
    @Bean
    public OpenAPI openApi() {
        // 创建一个新的 OpenAPI 对象
        return new OpenAPI()
                // 设置 OpenAPI 的信息
                .info(new Info()
                        // 设置 OpenAPI 的标题
                        .title("苍穹外卖项目接口文档")
                        // 设置 OpenAPI 的版本号
                        .version("2.0")
                        // 设置 OpenAPI 的描述
                        .description("苍穹外卖项目接口文档"))
                // 设置 OpenAPI 的安全要求
                .schemaRequirement("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }

    @Override
    // 重写extendMessageConverters方法，用于扩展消息转换器
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter= new MappingJackson2HttpMessageConverter();
        // 设置对象映射器，替换默认的对象映射器java-json
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将消息转换器对象添加到转换器列表中，并放在首位
        converters.addFirst(converter);
    }
}