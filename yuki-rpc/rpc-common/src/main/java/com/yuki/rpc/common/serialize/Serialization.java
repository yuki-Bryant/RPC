package com.yuki.rpc.common.serialize;

import com.yuki.rpc.common.RpcRequest;
import com.yuki.rpc.common.RpcResponse;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 16:22
 */
public class Serialization {
    private SerializeStrategy strategy;
    private static final Serialization DEFAULT_SERIALIZATIONY = new Serialization(new HessianStrategy());
    private Serialization(SerializeStrategy strategy) {
        this.strategy = strategy;
    }

    public static Serialization getDefaultSerialization() {
        return DEFAULT_SERIALIZATIONY;
    }

    public RpcRequest getRequest(byte[] bytes){
        RpcRequest request = null;
        try {
            request = (RpcRequest) strategy.bytesToObject(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return request;
    }

    public byte[] getBytes(RpcRequest request){
        byte[] bytes = null;
        try {
            bytes = strategy.objectToBytes(request);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }

    public RpcResponse getResponse(byte[] bytes){
        RpcResponse response = null;
        try {
            response = (RpcResponse) strategy.bytesToObject(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public byte[] getBytes(RpcResponse response){
        byte[] bytes = null;
        try {
            bytes = strategy.objectToBytes(response);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }

}
