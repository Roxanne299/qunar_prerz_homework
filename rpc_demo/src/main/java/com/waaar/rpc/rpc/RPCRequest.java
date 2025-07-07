package com.waaar.rpc.rpc;

import java.util.Arrays;

public class RPCRequest {
    private long requestId;
    private String serviceName;
    private String methodName;
    private String[] parameters;

    // getters and setters
    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RPCRequest{" +
                "requestId=" + requestId +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
