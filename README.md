# MyreCache
mybatis整合redis作为二级缓存
增加配置文件myrecache.properties即可使用 配置内容如下
#jedis连接池相关配置
#jedis连接池配置全部以模式pool.开始
#配置池容量上限
pool.maxTotal=30
#配置空闲连接上限
pool.maxIdle=10
#配置超时时间
pool.maxWaitMillis=500
#配置初始Jedis连接数量
pool.initialSize=

#Jedis连接Redis服务时相关配置
#redis服务器地址
hostName=192.168.168.168
#redis服务器端口
hostPort=6379
#redis授权口令(如果不需要授权则不需要配置)
auth=123456

XML中开启二级缓存即可
<cache type="com.lizhengpeng.myrecache.core.MyreCache" size="2048"/>

使用mybatis二级缓存时请注意多表更新操作时的脏读问题！！！！！！
