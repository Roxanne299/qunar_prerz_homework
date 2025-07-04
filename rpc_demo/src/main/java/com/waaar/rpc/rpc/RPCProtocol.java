package com.waaar.rpc.rpc;

import java.util.concurrent.atomic.AtomicLong;

public class RPCProtocol {

    public static final String RPC_PROTOCOL_SEP = "|";
    public static final String RPC_PROTOCOL_PARAMS_SEP = ",";
    public static final AtomicLong rpcRequestProtocolId = new AtomicLong(0);


    /**
     *  针对制定service method和参数编码成rpc定义的String格式
     * @param serviveName
     * @param methodName
     * @param params
     * @return
     */
    public static String encodeRequest(String serviveName,String methodName,Object... params){
        StringBuilder requestRPC = new StringBuilder("");
        requestRPC.append(serviveName);
        requestRPC.append(RPC_PROTOCOL_SEP);
        requestRPC.append(methodName);
        requestRPC.append(RPC_PROTOCOL_SEP);

        for(int i = 0;i < params.length;i++){
            requestRPC.append(params[i]);
            if(i < params.length - 1){
                requestRPC.append(RPC_PROTOCOL_PARAMS_SEP);
            }
        }

        return requestRPC.toString();
    }

    /**
     * 解码rpc格式 将消费者rpc请求解码成RPCRequest
     * @param requestRPC
     * @return
     */
    public static RPCRequest decodeRequest(String requestRPC){
        String[] parts = requestRPC.split("\\" + RPC_PROTOCOL_SEP);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid request format");
        }

        RPCRequest rpcRequest = new RPCRequest();
        rpcRequest.setRequestId(rpcRequestProtocolId.incrementAndGet());
        rpcRequest.setServiceName(parts[0]);
        rpcRequest.setMethodName(parts[1]);

        if (parts.length >= 3 && !parts[2].isEmpty()) {
            rpcRequest.setParameters(parts[2].split(RPC_PROTOCOL_PARAMS_SEP));
        }

        return rpcRequest;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {

        String s = encodeRequest("Add", "AddMath", 1, 2, 3);
        System.out.println(s);
        RPCRequest rpcRequest = decodeRequest(s);
        System.out.println(rpcRequest);
    }



    /**
     * 编码响应：requestId|result=value 或 requestId|error=message
     * @param rpcRequestId
     * @param result
     * @param error
     * @return
     */
    public static String encodeResponse(Long rpcRequestId,Object result,String error){
        StringBuilder sb = new StringBuilder();
        sb.append(rpcRequestId).append(RPC_PROTOCOL_SEP);
        if (error != null) {
            sb.append("error=").append(error.toString());
        } else {
            sb.append("result=").append(result.toString());
        }

        return sb.toString();
    }

    /**
     * 解码生产者rpc请求
     * @param responseRPC
     * @return
     */
    public static RPCResponse decodeResponse(String responseRPC){
        String[] parts = responseRPC.split("\\" + RPC_PROTOCOL_SEP);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid response format");
        }

        RPCResponse rpcResponse = new RPCResponse();
        rpcResponse.setRequestId(Long.parseLong(parts[0]));

        String resultPart = parts[1];
        if (resultPart.startsWith("error=")) {
            rpcResponse.setError(resultPart.substring(6));
        } else if (resultPart.startsWith("result=")) {
            rpcResponse.setResult(resultPart.substring(7));
        }

        return rpcResponse;
    }


}
