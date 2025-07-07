package com.waaar.rpc.rpc;

import java.io.IOException;
import java.util.concurrent.*;

public class RPCConsumer {
    public static void main(String[] args) throws IOException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.submit(new PRCConsumerThread());
        }
        threadPoolExecutor.shutdown();

    }
}
