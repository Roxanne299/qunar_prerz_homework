package com.waaar.sentinel.sentinel_server.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestLogInterceptor.class);

    private static final String START_TIME = "START_TIME";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        long duration = startTime == null ? -1 : (System.currentTimeMillis() - startTime);
        String threadName = Thread.currentThread().getName();
        String url = request.getRequestURI();
        String params = request.getQueryString();

        String state = request.getAttribute("BLOCKED") == null ? "正常" : (String) request.getAttribute("BLOCKED");
        logger.info("[RequestLog] 时间: {}, 线程: {}, URL: {}, 参数: {}, 耗时: {}ms, 状态: " + state,
                new java.util.Date(), threadName, url, params, duration);
    }
}
