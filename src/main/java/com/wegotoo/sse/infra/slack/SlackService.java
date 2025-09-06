package com.wegotoo.sse.infra.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackService {

    private final Slack slack = Slack.getInstance();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public void sendMessage(String message) {
        Payload payload = Payload.builder().text(message).build();

        try {
            slack.send(webhookUrl, payload);
            log.info("[INFO] Slack으로 DLQ 메시지 알림을 전송했습니다.");
        } catch (IOException e) {
            log.error("[ERROR] Slack 알림 전송에 실패했습니다.", e);
            throw new RuntimeException("Slack 알림 전송 실패", e);
        }
    }
}
