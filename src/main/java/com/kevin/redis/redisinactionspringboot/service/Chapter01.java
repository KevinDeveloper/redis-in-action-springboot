package com.kevin.redis.redisinactionspringboot.service;

import com.kevin.redis.redisinactionspringboot.constant.Constant;
import com.kevin.redis.redisinactionspringboot.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Chapter01
 * @Description:
 * @Author: Kevin
 * @Date: 2018/11/2 13:24
 */
@Slf4j
@Service
public class Chapter01 {


    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 给文章投票
     *
     * @param userId    用户id
     * @param articleId 文章id
     */
    public void articleVote(String userId, String articleId) {
        //判断当前文章是否支持投票
        double timeDiff = System.currentTimeMillis() - Constant.ONE_WEEK_IN_MINSECONDS;
        if (redisTemplate.opsForZSet().score("time", "article:" + articleId) + Constant.ONE_WEEK_IN_MINSECONDS < timeDiff) {
            return;
        }
        //判断该用户是否有投票
        String votedKey = "voted:" + articleId;
        String obj = "user:" + userId;
        if (!redisTemplate.opsForSet().isMember(votedKey, obj)) {
            //投票用户增加
            redisTemplate.opsForSet().add(votedKey, obj);
            //文章投票数增加
            redisTemplate.opsForZSet().incrementScore("article:" + articleId, "votes", 1);
            //文章评分增加
            redisTemplate.opsForZSet().incrementScore("score", articleId, Constant.VOTE_SCORE);
        }
    }

    /**
     * 发布文章
     *
     * @param userId
     * @param title
     * @param link
     */
    public void postArticle(String userId, String title, String link) {
        String articleId = UUIDUtil.uuid();
        String articlekey = "article:" + articleId;
        long curTime = System.currentTimeMillis();
        Map<String, Object> articleContent = new HashMap<>();
        articleContent.put("title", title);
        articleContent.put("link", link);
        articleContent.put("time", curTime);
        articleContent.put("poster", userId);
        articleContent.put("votes", 1);
        redisTemplate.opsForHash().putAll(articlekey, articleContent);
        String votedsKey = "voted:" + articleId;
        redisTemplate.opsForSet().add(votedsKey, articlekey);

        redisTemplate.opsForZSet().add("time", articlekey, curTime);
        redisTemplate.opsForZSet().add("score", articlekey, Constant.VOTE_SCORE);

    }

    /**
     * 获取文章列表
     *
     * @param page
     * @param order
     * @return
     */
    public List<Map<String, Object>> getArticles(int page, String order) {
        List<Map<String, Object>> articles = new ArrayList<>();
        //默认按评分排序
        order = "score";
        int startIndex = (page > 1 ? page - 1 : 0) * Constant.ARTICLES_PER_PAGE;
        int endIndex = startIndex + Constant.ARTICLES_PER_PAGE - 1;
        Set<String> articleIds = redisTemplate.opsForZSet().reverseRangeWithScores("score", startIndex, endIndex);
        for (String id : articleIds) {
            articles.add(redisTemplate.opsForHash().entries(id));
        }
        return articles;
    }

    /**
     * 更改文章的群组
     *
     * @param articleId
     * @param toAddGroupIds
     * @param toRemoveGroups
     */
    public void addRemoveGroups(String articleId, List<String> toAddGroupIds, List<String> toRemoveGroups) {
        String articleKey = "article:" + articleId;
        for (String groupId : toAddGroupIds) {
            redisTemplate.opsForSet().add("group:" + groupId, articleKey);
        }
        for (String groupId : toRemoveGroups) {
            redisTemplate.opsForSet().remove("group:" + groupId, articleKey);
        }
    }

    /**
     * 按群组获取文章
     *
     * @param groupId
     * @param page
     * @param order
     * @return
     */
    public List<Map<String, Object>> getGroupArticles(String groupId, int page, String order) {
        List<Map<String, Object>> articles = new ArrayList<>();
        order = "score";
        String groupOrderKey = order + groupId;
        if (!redisTemplate.hasKey(groupOrderKey)) {
            redisTemplate.opsForZSet().intersectAndStore(groupOrderKey, "group:" + groupId, order);
            redisTemplate.expire(groupOrderKey, 60, TimeUnit.SECONDS);
        }
        int startIndex = (page > 1 ? page - 1 : 0) * Constant.ARTICLES_PER_PAGE;
        int endIndex = startIndex + Constant.ARTICLES_PER_PAGE - 1;
        Set<String> articleIds = redisTemplate.opsForZSet().reverseRangeWithScores(groupOrderKey, startIndex, endIndex);
        for (String id : articleIds) {
            articles.add(redisTemplate.opsForHash().entries(id));
        }
        return articles;
    }


}
