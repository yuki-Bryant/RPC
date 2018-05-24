package com.yuki.rpc.common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * RPC请求消息结构
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 15:28
 */
public class RpcRequest implements Serializable{
    private String serviceName;
    private String methodName;
    private Object[] args;//方法所需参数
    private long requestId;

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

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "com.yuki.rpc.common.RpcRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
