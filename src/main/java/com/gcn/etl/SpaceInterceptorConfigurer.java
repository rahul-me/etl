package com.gcn.etl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SpaceInterceptorConfigurer extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

       registry.addInterceptor(spaceInterceptor()).addPathPatterns("/datauploaderetl/spaces/{spaceId}/**");
     }
    
    @Bean
    public SpaceInterceptor spaceInterceptor() {
        return new SpaceInterceptor();
    }

}
