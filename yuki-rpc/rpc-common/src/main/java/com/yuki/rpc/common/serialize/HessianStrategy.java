package com.yuki.rpc.common.serialize;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 序列化工具，序列化和反序列化,便于网络传输
 * hession不是说序列化的时候时间减少，而是序列化以后在网络上传输的时候能够减少时间(因为流中的字节数减少了一半)
 * Created by IntelliJ IDEA.
 * User: LHL
 * Date: 2018/5/22
 * Time: 16:00
 */
public class HessianStrategy implements SerializeStrategy {
    @Override
    public byte[] objectToBytes(Object o) throws IOException {
        if (o==null)
            throw new NullPointerException();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput output = new HessianOutput(os);
        output.writeObject(o);
        byte[] bytes = os.toByteArray();
        os.close();
        return bytes;
    }

    @Override
    public Object bytesToObject(byte[] bytes) throws IOException {
        if (bytes==null)
            throw new NullPointerException();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        HessianInput input = new HessianInput(inputStream);
        Object o = input.readObject();
        inputStream.close();
        return o;
    }
}
