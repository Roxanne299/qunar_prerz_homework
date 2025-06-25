# Sentinel Server

这是一个基于 Spring Boot 的简单示例，演示如何在接口中接入 Alibaba Sentinel 进行限流与熔断保护。

## 主要功能

- 暴露 `/api/data/process` REST 接口，模拟耗时处理并随机抛出异常
- 通过 `@SentinelResource` 注解，结合 `blockHandler` 和 `fallback` 实现限流与熔断后的降级逻辑
- `RequestLogInterceptor` 拦截并记录每次请求的耗时、状态（正常/限流/熔断）等信息

## 项目结构

```
sentinel_server
└── src
    └── main
        └── java
            └── com/waaar/sentinel/sentinel_server
                ├── config            # Spring MVC 配置
                ├── controller        # 接口及返回对象
                ├── exception         # 自定义异常
                └── interceptor       # 请求日志拦截器
```

## Sentinel 流控说明


## 运行示例



