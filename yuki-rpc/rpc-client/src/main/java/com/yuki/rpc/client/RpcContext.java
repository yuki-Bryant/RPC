package com.yuki.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/23
 * Time: 14:28
 */
public class RpcContext {
    private static RpcContext context = new RpcContext();

    public static RpcContext getInstance(){
        return context;
    }

    private Map<Thread,PromiseResponse> threadPromiseMap = new ConcurrentHashMap<>();

    public void addPromise(PromiseResponse response){
        threadPromiseMap.put(Thread.currentThread(),response);
    }

    public PromiseResponse getPromise(){
        return threadPromiseMap.get(Thread.currentThread());
    }
}
