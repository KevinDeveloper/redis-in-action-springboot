package com.kevin.redis.redisinactionspringboot.util;

import java.util.UUID;

/**
 * @Title: 
 * @Description: 
 * @author Kevin
 */ 
public class UUIDUtil {

    public static String uuid(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
