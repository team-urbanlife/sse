package com.wegotoo.sse.api.sse;

import com.wegotoo.sse.application.sse.SseService;
import com.wegotoo.sse.event.notification.request.NotificationMessage;
import com.wegotoo.sse.infra.resolvers.Auth;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
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

    @PostMapping(value = "/v1/send/{userId}")
    public void sendMessage(@PathVariable Long userId) {
        Random random = new Random();
        long randomNumber = random.nextInt(98) + 2;

        NotificationMessage message = NotificationMessage.builder()
                .fromId(randomNumber)
                .fromName("USER_" + randomNumber)
                .toId(userId)
                .toName("USER_" + userId)
                .event("COMMENT")
                .message("USER_" + randomNumber + "님이 댓글을 남겼습니다.")
                .build();

        rabbitTemplate.convertAndSend("app.sse", "sse", message);
    }

}
