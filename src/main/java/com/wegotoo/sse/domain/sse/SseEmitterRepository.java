package com.wegotoo.sse.domain.sse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    public void deleteByUserId(Long userId) {
        emitters.remove(userId);
    }

    public SseEmitter findByUserId(Long userId) {
        return emitters.get(userId);
    }

    public Map<Long, SseEmitter> getEmitters() {
        return emitters;
    }

}
