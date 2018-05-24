package com.yuki.rpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务的接口和服务的实现类
 * Service类内部能够通过method来路由（method通过接口的Class文件来解析），并执行方法
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 17:31
 */
public class Service {

    private static ServiceManager serviceManager = ServiceManager.getInstance();
    private Class<?> interfaceClass;
    private String serviceName;
    private Object impl;
    private Map<String,Method> methodMap = new HashMap<>();


    /**
     * 构造方法，解析interfaceClass下的方法
     * @param interfaceClass 方法的接口
     */
    public Service(Class<?> interfaceClass, Class<?> implClass){
        try {
            this.interfaceClass = interfaceClass;
            impl = implClass.newInstance();
            Method[] methods = interfaceClass.getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                methodMap.put(methodName, method);
            }
            serviceName = interfaceClass.getName();
            serviceManager.addService(this);
        } catch (Exception e) {
            System.out.println("类没有默认的构造方法");
        }
    }

    public Service(Class<?> interfaceClass, Object impl) {
        this.interfaceClass = interfaceClass;
        this.impl = impl;
        Method[] methods = interfaceClass.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            methodMap.put(methodName, method);
        }
    }

    public Service(String interfaceName, String implName) {
        try {
            Class interfaceClass;
            Class implClass;
            interfaceClass = Class.forName(interfaceName);
            this.interfaceClass = interfaceClass;
            implClass = Class.forName(implName);
            impl = implClass.newInstance();
            serviceName = interfaceName;
            Method[] methods = interfaceClass.getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                methodMap.put(methodName, method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object invoke(String methodName,Object...objects){
        Method method = methodMap.get(methodName);
        Object result = null;
        try {
            result = method.invoke(impl, objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getServiceName() {
        return serviceName;
    }
}
