package com.thesurvey.api.aspect;

import com.thesurvey.api.annotation.Lockable;
import com.thesurvey.api.domain.User;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.LockTimeoutExceptionMapper;
import com.thesurvey.api.util.UserUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Aspect for handling distributed locking using Redisson.
 */
@Aspect
@Component
public class LockAspect {

    private final RedissonClient redissonClient;

    public LockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(lockable)")
    public Object lock(ProceedingJoinPoint joinPoint, Lockable lockable) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = UserUtil.getUserFromAuthentication(authentication);
        long timeout = lockable.timeout();
        RLock lock = redissonClient.getLock(String.format("%s:%s", lockable.key(), user.getName()));
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(timeout, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new LockTimeoutExceptionMapper(ErrorMessage.LOCK_TIMEOUT);
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 중단되었습니다.", e);
        } finally {
            if (isLocked) {
                lock.unlock();
            }
        }
    }
}
