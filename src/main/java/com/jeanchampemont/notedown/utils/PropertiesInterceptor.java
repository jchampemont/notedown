package com.jeanchampemont.notedown.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PropertiesInterceptor extends HandlerInterceptorAdapter {

    @Value("${notedown.version}")
    private String notedownVersion;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null) {
            modelAndView.getModel().put("notedownVersion", notedownVersion);
        }
    }
}
