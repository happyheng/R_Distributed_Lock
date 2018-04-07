package com.happyheng.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

/**
 *
 * Created by happyheng on 2018/4/6.
 */
@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    @Autowired
    private Jedis jedis;

    /**
     * 分布式锁的超时时间，此设置为10s
     */
    private static final int DISTRIBUTED_LOCK_TIME = 10;

    /**
     * 默认最大等待时间，此为5000ms
     */
    private static final int DISTRIBUTED_DEFAULT_MAX_WAIT_TIME = 5000;

    /**
     * 默认单次等待时间，此为50ms
     */
    private static final int DISTRIBUTED_DEFAULT_PER_WAIT_TIME = 50;

    /**
     * 分布式锁中对应value的随机位数
     */
    private static final int DISTRIBUTED_KEY_SIGNATURE_LENGTH = 6;


    /**
     * 使用redis来获取分布式锁，返回的为当前设置的锁的签名，设置这个锁的签名是为了避免当前线程A拿到分布式锁后，阻塞时间过长，导致锁被超时删除
     * ，被线程B拿到，线程A执行完成之后，可能又把线程B的删除掉，导致线程C又进入。
     *
     * @param key 对应key
     * @return 但成功时返回的为当前设置的锁的签名, 失败时返回的为""空字符串
     */
    private String lock(String key) {

        // 得到一个随机的字符串
        String keySignature = getRandomNumber();

        long reply = jedis.setnx(key, keySignature);
        if (reply > 0) {
            // 设置过期时间
            jedis.expire(key, DISTRIBUTED_LOCK_TIME);
            return keySignature;
        }
        return "";
    }

    /**
     * 删除分布式锁，注意此会传入keySignature，防止出现上述的误删情况
     *
     * @param key          分布式锁的key
     * @param keySignature 分布式锁的value
     */
    private void unLock(String key, String keySignature) {

        String distributedLockValue = jedis.get(key);
        // 当相等的时候才会删除
        if (keySignature.equals(distributedLockValue)) {
            jedis.del(key);
        }
    }

    @Override
    public boolean tryLock(Runnable runnable, String key) {

        // 先获取分布式锁
        String keySignature = lock(key);
        if (StringUtils.isEmpty(keySignature)) return false;

        // 获取到分布式锁，开始执行任务
        runnable.run();
        // 执行完成后，删除分布式锁
        unLock(key, keySignature);
        return true;
    }

    @Override
    public boolean tryLock(Runnable runnable, String key, int maxWaitTimeMills, int perWaitTimeMills) {

        maxWaitTimeMills = maxWaitTimeMills <= 0 ? DISTRIBUTED_DEFAULT_MAX_WAIT_TIME : maxWaitTimeMills;
        perWaitTimeMills = perWaitTimeMills <= 0 || perWaitTimeMills >= maxWaitTimeMills ? DISTRIBUTED_DEFAULT_PER_WAIT_TIME : perWaitTimeMills;

        long beginTimeMillis = System.currentTimeMillis();
        // 尝试获取分布式锁，并执行任务
        while (!tryLock(runnable, key)) {
            // 当分布式锁失败时

            // 超过最大时间时，不再循环
            if (System.currentTimeMillis() - beginTimeMillis > maxWaitTimeMills) {
                return false;
            }

            // 等待指定时间
            try {
                Thread.sleep(perWaitTimeMills);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 得到分布式锁对应的value，解决阻塞或宕机造成的分布式锁失败的问题
     */
    private String getRandomNumber() {

        StringBuilder randomNumber = new StringBuilder();
        for (int i = 0; i < DISTRIBUTED_KEY_SIGNATURE_LENGTH; i ++) {
            randomNumber.append((int)(Math.random() * 10));
        }
        return randomNumber.toString();
    }

}
