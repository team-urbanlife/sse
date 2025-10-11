package com.wegotoo.sse.event.notification;

import static com.wegotoo.sse.infra.utils.SseMessagePath.DLQ_ROUTING_KEY;
import static com.wegotoo.sse.infra.utils.SseMessagePath.DLX_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.MAIN_EXCHANGE_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.MAIN_QUEUE_NAME;
import static com.wegotoo.sse.infra.utils.SseMessagePath.RETRY_ROUTING_KEY;

import com.rabbitmq.client.Channel;
import com.wegotoo.sse.application.sse.SseService;
import com.wegotoo.sse.event.notification.request.NotificationMessage;
import com.wegotoo.sse.infra.idempotency.Idempotent;
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

    @Idempotent
    @RabbitListener(queues = MAIN_QUEUE_NAME)
    public void handleNotificationMessage(
            Channel channel,
            NotificationMessage notificationMessage,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
            @Header(name = RETRY_COUNT_HEADER, required = false, defaultValue = "0") Integer retryCount
    ) throws IOException {
        try {
            sseService.send(notificationMessage);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (retryCount >= MAX_RETRIES) {
                sendToDLQ(notificationMessage);
            } else {
                sendToRetryQueue(notificationMessage, retryCount, messageId, correlationId);
            }

            channel.basicAck(deliveryTag, false);
        }
    }

    private void sendToDLQ(NotificationMessage notificationMessage) {
        rabbitTemplate.convertAndSend(DLX_NAME, DLQ_ROUTING_KEY, notificationMessage);
    }

    private void sendToRetryQueue(NotificationMessage notificationMessage, int currentRetryCount, String messageId,
                                  String correlationId) {
        final int newRetryCount = currentRetryCount + 1;

        rabbitTemplate.convertAndSend(MAIN_EXCHANGE_NAME, RETRY_ROUTING_KEY,
                notificationMessage, msg -> {
                    msg.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, newRetryCount);
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setCorrelationId(correlationId);

                    return msg;
                });
    }

}
