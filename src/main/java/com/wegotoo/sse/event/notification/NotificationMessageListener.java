package com.wegotoo.sse.event.notification;

import static com.wegotoo.sse.infra.utils.SseMessagePath.DLQ_ROUTING_KEY;
import static com.wegotoo.sse.infra.utils.SseMessagePath.DLX_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.MAIN_EXCHANGE_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.MAIN_QUEUE_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.RETRY_ROUTING_KEY;

import com.rabbitmq.client.Channel;
import com.wegotoo.sse.application.sse.SseService;
import com.wegotoo.sse.event.notification.request.NotificationMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    private final SseService sseService;
    private final RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRIES = 3;
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @RabbitListener(queues = MAIN_QUEUE_NAME)
    public void handleNotificationMessage(
            NotificationMessage notificationMessage, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(name = RETRY_COUNT_HEADER, required = false, defaultValue = "0") Integer retryCount
    ) throws IOException {
        try {
            sseService.send(notificationMessage);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (retryCount >= MAX_RETRIES) {
                sendToDLQ(notificationMessage);
            } else {
                sendToRetryQueue(notificationMessage, retryCount);
            }

            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendToDLQ(NotificationMessage notificationMessage) {
        rabbitTemplate.convertAndSend(DLX_NAME, DLQ_ROUTING_KEY, notificationMessage);
    }

    private void sendToRetryQueue(NotificationMessage notificationMessage, int currentRetryCount) {
        final int newRetryCount = currentRetryCount + 1;

        rabbitTemplate.convertAndSend(MAIN_EXCHANGE_NAME, RETRY_ROUTING_KEY,
                notificationMessage, msg -> {
                    msg.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, newRetryCount);
                    return msg;
                });
    }
}
