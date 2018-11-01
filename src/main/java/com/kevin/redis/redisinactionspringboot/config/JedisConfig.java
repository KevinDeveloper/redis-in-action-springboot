package com.kevin.redis.redisinactionspringboot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @ClassName: BatchJedisConfig
 * @Description:
 * @Author: Kevin
 * @Date: 2018/8/28 18:53
 */
@Configuration
public class JedisConfig {

    @Configuration
    @ConfigurationProperties(prefix = "spring.redis")
    public static class RedisConfig {
        @Setter
        public static String host;
        @Setter
        public static int port;

        public static String password;
        @Setter
        public static int database;

        public static void setPassword(String password) {
            if (StringUtils.isBlank(password)) {
                RedisConfig.password  = null;
            }else{
                RedisConfig.password = password;
            }
        }

    }

    @Configuration
    @ConfigurationProperties(prefix = "spring.redis.jedis.pool")
    public static class RedisPoolConfig {
        @Setter
        public static int maxIdle;
        @Setter
        public static int minIdle;
        @Setter
        public static int maxActive;
        @Setter
        public static long maxWait;


    }

    @Bean
    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        //指定连接池中最大空闲连接数
        config.setMaxIdle(RedisPoolConfig.maxIdle);
        config.setMinIdle(RedisPoolConfig.minIdle);
        //设置创建链接的超时时间
        config.setMaxWaitMillis(RedisPoolConfig.maxWait);
        //链接池中创建的最大连接数
        config.setMaxTotal(RedisPoolConfig.maxActive);
        //在borrow一个jedis实例时，是否提前进行validate操作；
        // 如果为true，则得到的jedis实例均是可用的；
        config.setTestOnBorrow(true);
        return config;
    }

    @Bean
    public JedisPool getJedisPool(JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, RedisConfig.host, RedisConfig.port, Protocol.DEFAULT_TIMEOUT, RedisConfig.password, RedisConfig.database, null);
        return jedisPool;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


}
