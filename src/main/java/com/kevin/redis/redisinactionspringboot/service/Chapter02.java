package com.kevin.redis.redisinactionspringboot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName: Chapter02
 * @Description:
 * @Author: Kevin
 * @Date: 2018/11/7 14:44
 */
@Slf4j
@Service
public class Chapter02 {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据token获取对应的userId
     *
     * @param token
     * @return
     */
    public String checkToken(String token) {
        return (String) redisTemplate.opsForHash().get("login:", token);
    }

    /**
     * 更新token
     *
     * @param token
     * @param userId
     * @param item
     */
    public void updateToken(String token, String userId, String item) {
        long curTime = System.currentTimeMillis();
        redisTemplate.opsForHash().put("login:", token, userId);
        redisTemplate.opsForZSet().add("recent:", token, curTime);
        if (StringUtils.isNotBlank(item)) {
            redisTemplate.opsForZSet().add("viewed:" + token, item, curTime);
            redisTemplate.opsForZSet().removeRangeByScore("viewed:" + token, 0, -26);
            redisTemplate.opsForZSet().add("viewed:", item, -1);
        }
    }

    /**
     * 清空session
     *
     * @param limitSize
     * @param isLimit
     */
    public void cleanSession(Long limitSize, boolean isLimit) {
        if (!isLimit) {
            return;
        }
        Long curUserSize = redisTemplate.opsForZSet().size("recent:");
        if (Objects.isNull(curUserSize) || curUserSize.longValue() < limitSize.longValue()) {
            return;
        }
        long diff = curUserSize.longValue() - limitSize.longValue();
        Set<String> tokens = redisTemplate.opsForZSet().range("recent:", 0, diff);
        if(!CollectionUtils.isEmpty(tokens)){
            return;
        }
        tokens = tokens.stream().map(token -> token = "Viewed:" + token).collect(Collectors.toSet());
        redisTemplate.delete(tokens);
        redisTemplate.opsForHash().delete("login:", tokens);
        redisTemplate.opsForZSet().remove("recent:", tokens);
    }

    /**
     * 添加商品到购物车
     * @param session
     * @param item
     * @param count
     */
    public void addToCart(String session, String item, int count) {
        if (count <= 0) {
            redisTemplate.opsForHash().delete("cart:" + session, item);
        } else {
            redisTemplate.opsForHash().put("cart:" + session, item, count);
        }

    }

    /**
     * 清空所有的session
     */
    public void cleanFullSession(Long limitSize, boolean isLimit){
        Long size = redisTemplate.opsForZSet().size("recent:");
        if(Objects.isNull(size) || size.longValue() < limitSize){
            return;
        }
        long diff = size - limitSize;
        Set<String> sessions = redisTemplate.opsForZSet().range("recent:", 0 , diff - 1);
        List<String> sessionKeys = new ArrayList<>();
        sessions.forEach(session -> {
            sessionKeys.add("viewed:" + session);
            sessionKeys.add("cart:" + session);
        });
        redisTemplate.delete(sessionKeys);
        redisTemplate.opsForHash().delete("login:", sessions);
        redisTemplate.opsForZSet().remove("recent:", sessions);

    }




}
