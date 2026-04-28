package com.campushub.order.service.impl;

import com.campushub.order.dto.GrabOrderRequest;
import com.campushub.order.lock.RedissonLock;
import com.campushub.order.service.GrabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrabServiceImpl implements GrabService {

    private final RedissonLock redissonLock;

    @Override
    public void grabOrder(GrabOrderRequest request) {
        String key = "task:grab:" + request.getTaskId();
        if (redissonLock.tryLock(key)) {
            redissonLock.unlock(key);
        }
    }
}