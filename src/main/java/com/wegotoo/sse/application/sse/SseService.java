package com.wegotoo.sse.application.sse;

import com.wegotoo.sse.application.sse.request.NotificationMessage;
import com.wegotoo.sse.domain.sse.SseEmitterRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 604800000L;
    private static final String DEFAULT_CONNECT_NAME = "PING";
    private static final String DEFAULT_CONNECT_MESSAGE = "connect";
    private static final String DEFAULT_HEARTBEAT_MESSAGE = "heartbeat";

    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        initializeEmitterEvents(userId, emitter);

        sseEmitterRepository.save(userId, emitter);

        sendConnectMessage(userId, emitter);
        return emitter;
    }

    private void sendConnectMessage(Long userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name(DEFAULT_CONNECT_NAME)
                    .data(DEFAULT_CONNECT_MESSAGE));
        } catch (IOException e) {
            sseEmitterRepository.deleteByUserId(userId);
        }
    }

    public void send(NotificationMessage message) {
        SseEmitter emitter = sseEmitterRepository.findByUserId(message.getToId());

        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name(message.getEvent())
                    .data(message));
        } catch (IOException e) {
            sseEmitterRepository.deleteByUserId(message.getToId());
        }
    }

    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        sseEmitterRepository.getEmitters().forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(DEFAULT_CONNECT_NAME)
                        .data(DEFAULT_HEARTBEAT_MESSAGE));
            } catch (IOException e) {
                sseEmitterRepository.deleteByUserId(id);
            }
        });
    }

    private void initializeEmitterEvents(Long userId, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            sseEmitterRepository.deleteByUserId(userId);
        });

        emitter.onTimeout(() -> {
            emitter.complete();
            sseEmitterRepository.deleteByUserId(userId);
        });

        emitter.onError((ex) -> {
            emitter.complete();
            sseEmitterRepository.deleteByUserId(userId);
        });
    }

}
