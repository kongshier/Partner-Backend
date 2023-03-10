package com.shier.partner.service;

import com.shier.partner.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author Shier
 */
@SpringBootTest
public class RedisTest {
    //RedisTemplate
    @Resource
    private RedisTemplate redisTemplate;
    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("shierString","dog");
        valueOperations.set("shierInt",1);
        valueOperations.set("shierDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("shier");
        valueOperations.set("shierUser",user);
        //查
        Object shier = valueOperations.get("shierString");
        Assertions.assertTrue("dog".equals((String)shier));
        shier = valueOperations.get("shierInt");
        Assertions.assertTrue(1==((Integer)shier));
        shier = valueOperations.get("shierDouble");
        Assertions.assertTrue(2.0==((Double)shier));
        System.out.println(valueOperations.get("shierUser"));
    }
}