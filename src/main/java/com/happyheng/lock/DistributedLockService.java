package com.happyheng.lock;

/**
 *
 * Created by happyheng on 2018/4/6.
 */
public interface DistributedLockService {

    boolean tryLock(Runnable runnable, String key);

    boolean tryLock(Runnable runnable, String key, int maxWaitTimeMills, int perWaitTimeMills);

}
