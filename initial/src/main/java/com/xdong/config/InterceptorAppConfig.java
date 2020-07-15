package com.xdong.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.xdong.interceptor.AuthenticationInterceptor;

@Component
public class InterceptorAppConfig extends WebMvcConfigurerAdapter {
   @Autowired
   AuthenticationInterceptor authenticationInterceptor;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
   }
}