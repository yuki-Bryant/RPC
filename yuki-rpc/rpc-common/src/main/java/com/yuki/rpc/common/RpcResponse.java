package com.yuki.rpc.common;

import java.io.Serializable;

/**
 * RPC响应消息结构
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 15:35
 */
public class RpcResponse implements Serializable {
    private int status;//表示请求的状态 200请求成功
    private Object result;//表示服务调用的结果
    private long requestId = -1;//请求id

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    //定义状态码
    public static final int OK = 200;
    public static final int SERVICE_NOT_FOUND = 404;
    public static final int PROVIDER_NOT_FOUND = 406;
    public static final int TIMEOUT = 407;
    public static final int THREAD_EXCEPTION = 408;
    public static final int BAD_TYPE = 409;
    public static final int SERVICE_OFFLINE = 410;

    public static RpcResponse OK(Object o, long requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatus(OK);
        response.setResult(o);
        return response;
    }

    public static RpcResponse ServiceNotFound(String serviceName, long requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatus(SERVICE_NOT_FOUND);
        response.setResult(serviceName);
        return response;
    }

    public static RpcResponse ProviderNotFound(String serviceName, long requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatus(PROVIDER_NOT_FOUND);
        response.setResult(serviceName);
        return response;
    }

    public static RpcResponse TimeOut(long requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatus(TIMEOUT);
        return response;
    }

    public static RpcResponse ThreadException(){
        RpcResponse response = new RpcResponse();
        response.setStatus(THREAD_EXCEPTION);
        return response;
    }

    /**
     * 服务端收到的请求不是RpcRequest类型
     * 或者客户端收到数据不是RpcResponse类型
     * @return
     */
    public static RpcResponse BadType(){
        RpcResponse response = new RpcResponse();
        response.setStatus(BAD_TYPE);
        return response;
    }

    public static RpcResponse ServiceOffline(String serviceName,long requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setStatus(SERVICE_OFFLINE);
        response.setResult(serviceName);
        return response;
    }
}
