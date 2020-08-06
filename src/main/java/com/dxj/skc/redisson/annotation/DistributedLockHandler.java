package com.dxj.skc.redisson.annotation;

import com.dxj.skc.exception.SkException;
import com.dxj.skc.redisson.RedissonLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @Description: Redisson 分布式锁注解解析器
 * @Author: Sinkiang
 * @Date: 2020/7/27 10:35
 * @CopyRight: 2020 sk-admin all rights reserved.
 */
@Aspect
@Component
@Slf4j
public class DistributedLockHandler {

    final RedissonLock redissonLock;

    public DistributedLockHandler(RedissonLock redissonLock) {
        this.redissonLock = redissonLock;
    }


    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        log.info("[开始]执行RedisLock环绕通知,获取Redis分布式锁开始");
        // 获取锁名称
        String lockName = distributedLock.value();
        // 获取超时时间，默认10秒
        int leaseTime = distributedLock.leaseTime();
        redissonLock.lock(lockName, leaseTime);
        try {
            log.info("获取Redis分布式锁[成功]，加锁完成，开始执行业务逻辑...");
            joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("获取Redis分布式锁[异常]，加锁失败", throwable);
            throwable.printStackTrace();
        } finally {
            // 如果该线程还持有该锁，那么释放该锁。如果该线程不持有该锁，说明该线程的锁已到过期时间，自动释放锁
            if (redissonLock.isHeldByCurrentThread(lockName)) {
                redissonLock.unlock(lockName);
            }
        }
        log.info("释放Redis分布式锁[成功]，解锁完成，结束业务逻辑...");
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new SkException("出现异常");
        }
    }
}
