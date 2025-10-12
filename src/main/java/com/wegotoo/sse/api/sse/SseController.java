package com.wegotoo.sse.api.sse;

import com.wegotoo.sse.application.sse.SseService;
import com.wegotoo.sse.event.notification.request.NotificationMessage;
import com.wegotoo.sse.infra.resolvers.Auth;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {

    private final SseService sseService;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping(value = "/v1/sse", produces = "text/event-stream")
    public SseEmitter connect(@Auth Long userId) {
        return sseService.connect(userId);
    }

    @GetMapping(value = "/v1/sse/{userId}", produces = "text/event-stream")
    public SseEmitter connectionCopy(@PathVariable Long userId) {
        return sseService.connect(userId);
    }

}
