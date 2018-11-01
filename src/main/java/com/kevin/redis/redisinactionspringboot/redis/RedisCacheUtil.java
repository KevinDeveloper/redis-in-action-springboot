package com.kevin.redis.redisinactionspringboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: RedisService
 * @Description: redis 常规操作 简单封装
 * @Author: Kevin
 * @Date: 2018/4/26 09:30
 */
@Service
public class RedisCacheUtil {

    /**
     * redis 操作
     * <p>
     * redisTemplate.opsForValue();//操作字符串
     * redisTemplate.opsForHash();//操作hash
     * redisTemplate.opsForList();//操作list
     * redisTemplate.opsForSet();//操作set
     * redisTemplate.opsForZSet();//操作有序set
     */
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 操作String数据类型
     * StringRedisTemplate是RedisTemplate的唯一子类。
     */
    @Autowired
    public StringRedisTemplate stringRedisTemplate;


    //--------------------------------------------------------string

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return 缓存的对象
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value);
        return operation;
    }

    public <T> ValueOperations<String, T> setCacheObject(String key, T value, long expireTime, TimeUnit unit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value, expireTime, unit);
        return operation;
    }

    public <T> ValueOperations<String, T> incrementCacheObject(String key, long value, long expireTime, TimeUnit unit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.increment(key, value);
        redisTemplate.expire(key, expireTime, unit);
        return operation;
    }

    public ValueOperations<String, Object> multiSetCacheObject(Map<String, Object> map, long expireTime, TimeUnit unit) {
        ValueOperations<String, Object> operation = redisTemplate.opsForValue();
        operation.multiSet(map);
        for (String key : map.keySet()) {
            redisTemplate.expire(key, expireTime, unit);
        }
        return operation;
    }

    public List<Object> multiGetCacheObject(List<String> keyList) {
        ValueOperations<String, Object> operation = redisTemplate.opsForValue();
        List<Object> val = operation.multiGet(keyList);
        return val;
    }


    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }


    //--------------------------------------------list

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            listOperation.leftPushAll(key, dataList);
        }
        return listOperation;
    }

    /**
     * 该方法需要在事务中执行，由于push 和 expire是两条语句 ，会发起两次redis请求
     *
     * @param key
     * @param dataList
     * @param expireTime
     * @param unit
     * @param <T>
     * @return
     */
    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList, long expireTime, TimeUnit unit) {
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            dataList = new ArrayList<>();
            listOperation.leftPushAll(key, dataList);
            redisTemplate.expire(key, expireTime, unit);
        }
        return listOperation;
    }

    public <T> ListOperations<String, T> setCacheListReset(String key, List<T> dataList, long expireTime, TimeUnit unit) {
        if (exists(key)) {
            delete(key);
        }
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            listOperation.leftPushAll(key, dataList);
            redisTemplate.expire(key, expireTime, unit);
        }
        return listOperation;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(String key) {
        List<T> dataList = new ArrayList<T>();
        ListOperations<String, T> listOperation = redisTemplate.opsForList();
        long size = listOperation.size(key);
        dataList = listOperation.range(key, 1, size - 1);
        return dataList;
    }

    //------------------------------------------------------ set

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public void setCacheSet(String key, Set dataSet) {
        SetOperations setOperation = redisTemplate.opsForSet();
        if (null != dataSet) {
            setOperation.add(key, dataSet.toArray());
        }
    }

    public void setCacheSet(String key, Set dataSet, long expireTime, TimeUnit unit) {
        SetOperations setOperation = redisTemplate.opsForSet();
        if (null != dataSet) {
            setOperation.add(key, dataSet.toArray());
            redisTemplate.expire(key, expireTime, unit);
        }

    }


    public void addCacheSet(String key, Object object) {
        SetOperations setOperation = redisTemplate.opsForSet();
        setOperation.add(key, object);
    }

    public void addCacheSet(String key, Object object, long expireTime, TimeUnit unit) {
        SetOperations setOperation = redisTemplate.opsForSet();
        setOperation.add(key, object);
        redisTemplate.expire(key, expireTime, unit);
    }


    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(String key) {
        Set<T> dataSet = new HashSet<T>();
        SetOperations setOperation = redisTemplate.opsForSet();
        dataSet = setOperation.members(key);
        return dataSet;
    }

    public void removeCacheSet(String key, Object... values) {
        SetOperations setOperation = redisTemplate.opsForSet();
        setOperation.remove(key, values);
    }

    //------------------------------------------------map

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     * @return
     */
    public void setCacheMap(String key, Map dataMap) {
        HashOperations hashOperation = redisTemplate.opsForHash();
        if (null != dataMap) {
            hashOperation.putAll(key, dataMap);
        }
    }

    public void setCacheMap(String key, Map dataMap, long expireTime, TimeUnit unit) {
        HashOperations hashOperation = redisTemplate.opsForHash();
        if (null != dataMap) {
            hashOperation.putAll(key, dataMap);
            redisTemplate.expire(key, expireTime, unit);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(String key) {
        Map<String, T> map = redisTemplate.opsForHash().entries(key);
        return map;
    }

    //---------------------------------------------------

    /**
     * 删除指定key
     *
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }


    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取key值的过期时间
     *
     * @param key
     * @return -2: key值不存在， -1:未设置过期时间，否则返回剩余过期时间
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 给key设置过期时间
     *
     * @param key
     * @param expireTime
     * @param unit
     * @return
     */
    public boolean setExpire(String key, long expireTime, TimeUnit unit) {
        return redisTemplate.expire(key, expireTime, unit);
    }

    /**
     * 使用redies作分布式锁
     * 当key不存在时插入，存在时插入失败
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return true插入成功， false 插入失败
     */
    public boolean setIfAbsentCacheObject(String key, Object value) {
        ValueOperations<String, Object> operation = redisTemplate.opsForValue();
        return operation.setIfAbsent(key, value);

    }

    /**
     * 处理分布式锁，如果存在锁, 并且锁的过期时间没有过期时间，则给锁设置一个过期时间
     * 防止分布式锁不过期，不进行自动释放
     * getExpire TTL以毫秒为单位。 -2: 键不存在,  -1: key没有到期超时。
     *
     * @param key
     * @param expireTime
     * @param timeUnit
     */
    public void handleDistributedLock(String key, long expireTime, TimeUnit timeUnit) {
        long ttl = getExpire(key);
        // if (exists(key) && getExpire(key) < 0) {
        if (ttl < 0 && ttl != -2) {
            setExpire(key, expireTime, timeUnit);

        }

    }

    /**
     * 设置分布式锁，通过使用handleDistributedLock的方式给锁补偿增加过期时间，来保住分布式锁会被设置自动过期
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     * @return
     */
    public boolean getDistributedLock(String key, Object value, long expireTime, TimeUnit timeUnit) {
        handleDistributedLock(key, expireTime, timeUnit);
        boolean result = setIfAbsentCacheObject(key, value);
        if (result) {
            setExpire(key, expireTime, timeUnit);
        }
        return result;

    }


}
