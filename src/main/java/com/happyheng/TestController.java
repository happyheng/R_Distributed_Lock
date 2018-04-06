package com.happyheng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Created by happyheng on 2018/4/1.
 */
@RestController
public class TestController {

    @Autowired
    private Jedis jedis;

    @RequestMapping("/test")
    public String testRequest(HttpServletRequest httpServletRequest) {
        return "TestController";
    }

    @RequestMapping("/testRedis")
    public String testRedis(HttpServletRequest httpServletRequest) {
        return "Server is running: " + jedis.ping();
    }


    public static void main(String[] args) {
        // 连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("Connection to server sucessfully");
        // 查看服务是否运行
        System.out.println("Server is running: " + jedis.ping());
    }

}
