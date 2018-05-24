package com.yuki.rpc.client;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 21:52
 */
public class StringUtil {
    /**
     * 根据service路径获得对应的serviceName
     * 服务的路径的一般为/services/serviceName
     *
     * @param servicePath 服务在zookeeper上存储的路径
     * @return 对应的服务名
     */
    public static String getServiceName(String servicePath) {
        String[] strings = servicePath.split("/");
        return strings[2];
    }
}
