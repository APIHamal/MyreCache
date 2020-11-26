package com.lizhengpeng.myrecache.core;

import com.lizhengpeng.myrecache.util.ReflectUtil;
import com.lizhengpeng.myrecache.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;

/**
 * Jedis连接工厂类
 * @author lizhengpeng
 */
public class JedisFactory {

    private static Logger logger = LoggerFactory.getLogger(JedisFactory.class);

    private static ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<>();
    private static JedisPool jedisPool;
    private static final int INITSUCCESS = 1;
    private static final int INITFAIL = 2;
    private static int INIT_STATE = INITSUCCESS;
    private static final String PROP_NAME = "myrecache.properties";
    private static final String POOL_PREFIX = "pool.";
    private static final int TIMEOUT = 1000;

    /**
     * 初始化Jedis连接工厂
     */
    static {
        try{
            InputStream inputStream = ReflectUtil.getClassPathResource(PROP_NAME);
            if(inputStream == null){
                throw new FileNotFoundException("未发现配置文件["+PROP_NAME+"]");
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            //获取Redis服务器地址
            String hostName = properties.getProperty("hostName");
            if(StringUtil.isEmpty(hostName)){
                throw new IllegalArgumentException("请配置有效的redis服务器地址");
            }
            //获取Redis服务器端口
            String hostPort = properties.getProperty("hostPort");
            if(!StringUtil.isNumber(hostPort)){
                throw new IllegalArgumentException("请配置有效的redis服务器端口");
            }
            //获取Redis服务器的授权密码
            String auth = properties.getProperty("auth");
            //配置Jedis连接池
            JedisPoolConfig config = new JedisPoolConfig();
            autoConfigJedisPool(config, properties);
            //配置Jedis连接
            if(StringUtil.isEmpty(auth)){
                jedisPool = new JedisPool(config, hostName, Integer.parseInt(hostPort), TIMEOUT);
            }else{
                //配置Jedis连接池(连接redis时需要授权)
                jedisPool = new JedisPool(config, hostName, Integer.parseInt(hostPort), TIMEOUT, auth);
            }
        }catch (Throwable e){
            logger.error("初始化连接工厂失败", e);
            INIT_STATE = INITFAIL;
        }
    }

    /**
     * 反射的方式配置JedisPool相关属性
     * @param jedisPoolConfig
     * @param properties
     */
    private static void autoConfigJedisPool(JedisPoolConfig jedisPoolConfig, Properties properties){
        try{
            Set<String> propNameSet = properties.stringPropertyNames();
            if(!propNameSet.isEmpty()){
                Method[] methods = JedisPoolConfig.class.getMethods();
                for(String propName : propNameSet){
                    if(propName.startsWith(POOL_PREFIX)){
                        String actualProp = propName.substring(POOL_PREFIX.length());
                        String propVal = properties.getProperty(propName);
                        if(!StringUtil.isEmpty(actualProp) && !StringUtil.isEmpty(propVal)){
                            //拼接当前属性对应的Set方法
                            String methodName = "set"+actualProp.substring(0,1).toUpperCase() + actualProp.substring(1);
                            logger.debug("配置JedisPool属性使用的SET方法为["+methodName+"]");
                            //如果存在Set方法则设置并且参数长度为一
                            //标准的JavaBean满足上述规则
                            boolean findMethod = false;
                            setValue:for(Method method : methods){
                                Class<?>[] parameterArray = method.getParameterTypes();
                                if(method.getName().equals(methodName) && parameterArray.length == 1){
                                    logger.debug("调用JedisPool方法["+methodName+"]进行赋值-->值为["+propVal+"]");
                                    findMethod = true;
                                    try{
                                        method.invoke(jedisPoolConfig,valueAdapter(parameterArray[0],propVal));
                                    }catch (Throwable e){
                                        logger.debug("反射配置JedisPool出现异常", e);
                                    }finally {
                                        break setValue;
                                    }
                                }
                            }
                            //如果对应的Method未发线则进行提示
                            if(!findMethod){
                                logger.info("未发现指定的赋值方法["+methodName+"]忽略该属性的配置");
                            }
                        }
                    }
                }
            }
        }catch (Throwable t){
            logger.error("配置JedisPool出现异常", t);
        }
    }

    /**
     * 尝试将字符串内容转换成适当的类型
     * @param destType
     * @param valText
     * @return
     */
    private static Object valueAdapter(Class<?> destType, String valText){
        if(destType == int.class || destType == Integer.class){
            return Integer.valueOf(valText);
        }else if(destType == long.class || destType == Long.class){
            return Long.valueOf(valText);
        }else if(destType == boolean.class || destType == Boolean.class){
            return Boolean.valueOf(valText);
        }else if(destType == String.class){
            return valText;
        }else{
            throw new IllegalArgumentException("不受支持的类型转换["+destType.getName()+"]");
        }
    }

    /**
     * 获取Jedis连接对象
     * @return
     */
    public static Jedis getJedisConnect(){
        if(INIT_STATE == INITFAIL){
            return null;
        }
        Jedis client = jedisThreadLocal.get();
        if(client == null){
            client = jedisPool.getResource();
            jedisThreadLocal.set(client);
        }
        return client;
    }

    /**
     * 释放获取到的Jedis连接
     */
    public static void releaseJedis(){
        if(INIT_STATE == INITFAIL){
            return;
        }
        Jedis client = jedisThreadLocal.get();
        if(client != null){
            jedisThreadLocal.remove();
            try{
                client.close();
            }catch (Throwable e){
                logger.error("释放Jedis连接时发生异常", e);
            }
        }
    }

}
