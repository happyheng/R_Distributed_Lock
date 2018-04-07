package com.happyheng;

import com.happyheng.lock.DistributedLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Created by happyheng on 2018/4/6.
 */
@RestController
@RequestMapping("/lock")
public class LockController {

    @Autowired
    private DistributedLockService distributedLockService;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String testDistributedLock(HttpServletRequest httpServletRequest) {

        // 测试分布式锁
        distributedLockService.tryLock(new Runnable() {
            @Override
            public void run() {

                String threadName = Thread.currentThread().getName();
                System.out.println("线程" + threadName + "启动");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("线程" + threadName + "结束");

            }
        }, "lock_test", 5000, 100);

        return "";
    }



}
