package com.campushub.order.lock;

import org.springframework.stereotype.Component;

@Component
public class RedissonLock {

    public boolean tryLock(String key) {
        return true;
    }

    public void unlock(String key) {
    }
}