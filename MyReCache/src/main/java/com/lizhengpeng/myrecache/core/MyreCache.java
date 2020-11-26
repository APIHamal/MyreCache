package com.lizhengpeng.myrecache.core;

import com.lizhengpeng.myrecache.util.SerializeUtil;
import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Mybatis整合Redis实现二级缓存
 * @author lizhengpeng
 */
public class MyreCache implements Cache {

    private Logger logger = LoggerFactory.getLogger(MyreCache.class);

    /**
     * 记录当前缓存所属的命名空间
     */
    private String cacheNamespace;

    public MyreCache(String cacheNamespace){
        logger.debug("创建缓存命名空间["+cacheNamespace+"]");
        this.cacheNamespace = cacheNamespace;
    }

    @Override
    public String getId() {
        logger.debug("返回缓存所属命名空间("+cacheNamespace+")");
        return cacheNamespace;
    }

    @Override
    public void putObject(Object key, Object value) {
        logger.debug("缓存键 ("+key.toString()+")");
        logger.debug("缓存值 ("+value.toString()+")");
        if(!(key instanceof Serializable) || !(value instanceof Serializable)){
            logger.error("缓存对象必须实现Serializable接口");
            return;
        }
        try{
            Jedis client = JedisFactory.getJedisConnect();
            if(client != null){
                //序列化键值对保存进入redis中
                String keySer = SerializeUtil.writeObject(key);
                String valSer = SerializeUtil.writeObject(value);
                //保存到Redis的Hash表中
                //Hash以当前命名空间命名
                client.hset(cacheNamespace, keySer, valSer);
                logger.debug("缓存对象数据成功");
            }
        }catch (Throwable e){
            logger.error("缓存数据发生异常", e);
        }finally {
            JedisFactory.releaseJedis();
        }
    }

    @Override
    public Object getObject(Object key) {
        logger.debug("查询缓存 ("+key.toString()+")");
        try{
            Jedis client = JedisFactory.getJedisConnect();
            if(client != null){
                //序列化键值对保存进入redis中
                String keySer = SerializeUtil.writeObject(key);
                //如果键存在则反序列对象
                if(client.hexists(cacheNamespace, keySer)){
                    logger.debug("查询缓存命中");
                    String text = client.hget(cacheNamespace, keySer);
                    Object object = SerializeUtil.readObject(text);
                    return object;
                }
            }
        }catch (Throwable e){
            logger.error("查询缓存时发生异常", e);
        }finally {
            JedisFactory.releaseJedis();
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        logger.debug("删除缓存 ("+key.toString()+")");
        try{
            Jedis client = JedisFactory.getJedisConnect();
            if(client != null){
                String keySer = SerializeUtil.writeObject(key);
                client.hdel(cacheNamespace, keySer);
            }
        }catch (Throwable e){
            logger.error("删除缓存时发生异常", e);
        }finally {
            JedisFactory.releaseJedis();
        }
        return null;
    }

    @Override
    public void clear() {
        logger.debug("刷新缓存");
        try{
            Jedis client = JedisFactory.getJedisConnect();
            if(client != null){
                client.del(cacheNamespace);
            }
        }catch (Throwable e){
            logger.error("刷新缓存时发生异常", e);
        }finally {
            JedisFactory.releaseJedis();
        }
    }

    @Override
    public int getSize() {
        logger.debug("获取缓存数量");
        try{
            Jedis client = JedisFactory.getJedisConnect();
            if(client != null){
                long hlen = client.hlen(cacheNamespace);
                return (int)hlen;
            }
        }catch (Throwable e){
            logger.error("刷新缓存时发生异常", e);
        }finally {
            JedisFactory.releaseJedis();
        }
        return 0;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        throw new UnsupportedOperationException("不支持该加锁操作");
    }
}
