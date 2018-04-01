package com.happyheng;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Created by happyheng on 2018/4/1.
 */
@RestController
public class TestController {


    @RequestMapping("/test")
    public String testRequest(HttpServletRequest httpServletRequest) {
        return "TestController";
    }

}
