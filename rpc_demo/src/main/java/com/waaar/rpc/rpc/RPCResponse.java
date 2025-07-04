package com.waaar.rpc.rpc;

public class RPCResponse {
    private long requestId;
    private String result;
    private String error;
    public RPCResponse() {}
    public RPCResponse(long requestId, String result, String error) {
        this.requestId = requestId;
        this.result = result;
        this.error = error;
    }

    // getters and setters
    public long getRequestId() { return requestId; }

    public void setRequestId(long requestId) { this.requestId = requestId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
