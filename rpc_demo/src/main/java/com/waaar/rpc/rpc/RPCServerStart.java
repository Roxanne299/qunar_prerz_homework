package com.waaar.rpc.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class RPCServerStart {

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        RPCServer server = new RPCServer();
        server.start(8081);
    }
}
