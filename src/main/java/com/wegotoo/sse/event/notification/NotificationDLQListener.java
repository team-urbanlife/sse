package com.wegotoo.sse.event.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.wegotoo.sse.event.notification.request.NotificationMessage;
import com.wegotoo.sse.infra.slack.SlackService;
import com.wegotoo.sse.infra.utils.SseMessagePath;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDLQListener {

    private final SlackService slackService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = SseMessagePath.DLQ_NAME)
    public void handleDLQMessage(NotificationMessage failedMessage,
                                 Channel channel,
                                 @Header(name = AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        String messageJson = convertMessageToJson(failedMessage);
        String slackMessage = createAlertMessage(messageJson);

        slackService.sendMessage(slackMessage);
        channel.basicAck(deliveryTag, false);
    }

    private String convertMessageToJson(NotificationMessage message) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("[ERROR] DLQ 메시지를 JSON으로 변환하는 데 실패했습니다.", e);
            return "메시지 JSON 변환 실패: " + message.toString();
        }
    }

    private String createAlertMessage(String message) {
        return String.format("🚨 DLQ 수신 🚨\n최종 처리에 실패한 메시지가 있습니다.\n\n```\n%s\n```", message);
    }
}
