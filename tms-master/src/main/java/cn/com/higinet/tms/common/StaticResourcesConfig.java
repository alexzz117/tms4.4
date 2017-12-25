package cn.com.higinet.tms.common;

import cn.com.higinet.tms.common.filter.StaticResourcesFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class StaticResourcesConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/static/");
//        registry.addResourceHandler("/tms/static/**").addResourceLocations("classpath:/static/static/");
        super.addResourceHandlers(registry);
    }

    @Bean
    public FilterRegistrationBean testFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new StaticResourcesFilter());//注册rewrite过滤器
        registration.addUrlPatterns("/*");
        registration.setName("rewriteFilter");
        registration.setOrder(1);
        return registration;
    }

}
