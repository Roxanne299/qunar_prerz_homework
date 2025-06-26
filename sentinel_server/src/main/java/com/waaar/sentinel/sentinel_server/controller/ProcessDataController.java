package com.waaar.sentinel.sentinel_server.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.waaar.sentinel.sentinel_server.exception.ConsumerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

@RestController
public class ProcessDataController {

    @GetMapping("/api/data/process")
    @SentinelResource(value = "process",blockHandler = "qps",fallback = "baseReturn")
    public HttpResponse process() throws InterruptedException, ConsumerException {
        // 模拟业务逻辑 在100ms - 300ms中随机耗时
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        double randomDuoble = random.nextDouble();
        int waitMS = (int) (randomDuoble * 1000) % 200 + 100;
        Thread.sleep(waitMS);

        // 模拟随机出现异常
        int randomInt = random.nextInt(10);
        if(randomInt > 3) {
            throw new ConsumerException("模拟随机异常");
        }
        return new HttpResponse(200,"process");
    }

    public HttpResponse qps(BlockException ex){
        // 关键：通过 RequestContextHolder 获取当前请求并打标
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String blockType = ex instanceof com.alibaba.csp.sentinel.slots.block.degrade.DegradeException ? "熔断" : "限流";
            request.setAttribute("BLOCKED", blockType);
        }
        if (ex instanceof FlowException) {
            return new HttpResponse(429, "请求被限流啦！");
        }
        return new HttpResponse(500, "熔断中...服务繁忙");

    }

    public HttpResponse baseReturn(Throwable e){
        return new HttpResponse(500, e.getMessage());
    }


}
