package com.lizhengpeng.myrecache.util;

import java.io.InputStream;

/**
 * 反射相关工具类
 * @author lizhengpeng
 */
public class ReflectUtil {

    /**
     * 获取当前可用的类加载器
     * @return
     */
    public static ClassLoader getClassLoader(){
        if(Thread.currentThread().getContextClassLoader() != null){
            return Thread.currentThread().getContextClassLoader();
        }else if(ReflectUtil.class.getClassLoader() != null){
            return ReflectUtil.class.getClassLoader();
        }else{
            return ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * 获取ClassPath路径下的资源
     * @param resourceName
     * @return
     */
    public static InputStream getClassPathResource(String resourceName){
        if(StringUtil.isEmpty(resourceName)){
            throw new IllegalArgumentException("resourceName is empty");
        }
        return getClassLoader().getResourceAsStream(resourceName);
    }

    /**
     * 根据类全限定名加载类
     * @param className
     * @return
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        if(StringUtil.isEmpty(className)){
            throw new IllegalArgumentException("className is empty");
        }
        return getClassLoader().loadClass(className);
    }

}
