package com.waaar.rpc.service.impl;

import com.waaar.rpc.service.MathService;

public class MathServiceImpl implements MathService {
    @Override
    public int add(int a, int b) {
        try {
            // 模拟业务处理时间
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return a + b;
    }

    @Override
    public int multiply(int a, int b) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return a * b;
    }

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
