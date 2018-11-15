package com.kevin.redis.redisinactionspringboot.constant;

/**
 * @ClassName: Constant
 * @Description:
 * @Author: Kevin
 * @Date: 2018/11/15 10:17
 */
public interface Constant {
    /**
     * 一周的毫秒
     */
    long ONE_WEEK_IN_MINSECONDS = 7 * 24 * 60 * 60 * 1000;
    /**
     * 一次投票分数
     */
    int VOTE_SCORE = 5;
    /**
     * 文章列表分页
     */
    int ARTICLES_PER_PAGE = 25;
}
