package com.yuki.rpc.common.serialize;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 15:59
 */
public interface SerializeStrategy {
    //定义编解码方式,此处用的hessian
    byte[] objectToBytes(Object o) throws IOException;
    Object bytesToObject(byte[] bytes) throws IOException;
}
