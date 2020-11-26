package com.lizhengpeng.myrecache.util;


import org.apache.commons.codec.binary.Base64;

import java.io.*;

/**
 * 对象序列化/反序列化
 * @author lizhengpeng
 */
public class SerializeUtil {

    /**
     * 序列化对象
     * @param object
     * @return
     */
    public static String writeObject(Object object) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(os);
        outputStream.writeObject(object);
        outputStream.close();
        return Base64.encodeBase64String(os.toByteArray());
    }

    /**
     * 反序列化对象
     * @param content
     * @return
     */
    public static Object readObject(String content) throws IOException, ClassNotFoundException {
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decodeBase64(content));
        ObjectInputStream inputStream = new ObjectInputStream(is);
        Object object = inputStream.readObject();
        return object;
    }

}
