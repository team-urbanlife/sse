package com.wegotoo.sse.api.sse;

import com.wegotoo.sse.application.sse.SseService;
import com.wegotoo.sse.infra.resolvers.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/v1/sse", produces = "text/event-stream")
    public SseEmitter connect(@Auth Long userId) {
        return sseService.connect(userId);
    }

}
