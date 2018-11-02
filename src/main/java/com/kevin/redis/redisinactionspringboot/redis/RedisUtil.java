package com.kevin.redis.redisinactionspringboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * @ClassName: RedisUtil
 * @Description: redis 操作工具类
 * @Author: Kevin
 * @Date: 2018/11/2 14:20
 */
@Service
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    //--------------------------------------------List

    //--------------------------------------------Hash

    /**
     * hash存储key加1
     * @param key
     * @param hashKey
     * @param delta
     */
    public void incrementHash(String key, String hashKey, long delta ){
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.increment(key, hashKey, delta);
    }

    //--------------------------------------------Set

    /**
     * 增加set成员
     * @param key
     * @param object
     */
    public void addCacheSet(String key, Object object) {
        SetOperations setOperation = redisTemplate.opsForSet();
        setOperation.add(key, object);
    }

    /**
     * 判断是否为set集合成员
     *
     * @param key
     * @param object
     * @return
     */
    public boolean isMemberSet(String key, Object object) {
        SetOperations setOperations = redisTemplate.opsForSet();
        return setOperations.isMember(key, object);
    }



    //--------------------------------------------ZSet

    /**
     * 添加Zset
     *
     * @param key
     * @param member
     * @param score
     */
    public void addCacheZSet(String key, String member, double score) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(key, member, score);
    }

    /**
     * 获取分值
     *
     * @param key
     * @param member
     * @return
     */
    public double getCacheZSetScore(String key, String member) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.score(key, member);
    }

    /**
     * 增加分值
     * @param key
     * @param member
     * @param delta
     */
    public void incremCountZSet(String key, String member, double delta) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.incrementScore(key, member, delta);
    }


    //----------------------------------------------------


}
