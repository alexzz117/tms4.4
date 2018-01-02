package cn.com.higinet.tms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
//@Profile(value = "prod") 
public class WebSecurityConfig extends WebMvcConfigurerAdapter {
    
    //@Autowired
    //public LoginInterceptor loginInterceptor;
    
    public void addInterceptors(InterceptorRegistry registry) {
        /*InterceptorRegistration addInterceptor = registry.addInterceptor(loginInterceptor);

        // 排除配置
        addInterceptor.excludePathPatterns("/error");
        //先开放api这块 
        addInterceptor.excludePathPatterns("/doc**");
        addInterceptor.excludePathPatterns("/swagger**");
        addInterceptor.excludePathPatterns("/swagger-resources/configuration/**");
        addInterceptor.excludePathPatterns("/swagger-resources/configuration/ui");
        addInterceptor.excludePathPatterns("/webjars**");

        // 拦截配置
        addInterceptor.addPathPatterns("/**");*/
    }
    
}

