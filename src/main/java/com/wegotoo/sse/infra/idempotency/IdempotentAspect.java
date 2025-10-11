package com.wegotoo.sse.infra.idempotency;

import com.rabbitmq.client.Channel;
import com.wegotoo.sse.exception.IdempotencyException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final IdempotentRepository idempotentRepository;

    @Around("@annotation(com.wegotoo.sse.infra.idempotency.Idempotent)")
    public Object validateIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        Map<String, Object> parameters = getParameters(joinPoint);

        String messageId = getParameter(parameters, "messageId", String.class);
        String correlationId = getParameter(parameters, "correlationId", String.class);

        Boolean isSet = idempotentRepository.setIfAbsent(messageId, correlationId);
        if (isSet) {
            return joinPoint.proceed();
        }

        String storedCorrelationId = idempotentRepository.findByMessageId(messageId);
        if (Objects.equals(correlationId, storedCorrelationId)) {
            return joinPoint.proceed();
        }

        long deliveryTag = getParameter(parameters, "deliveryTag", Long.class);
        Channel channel = getParameter(parameters, "channel", Channel.class);

        channel.basicAck(deliveryTag, false);
        throw new IdempotencyException(messageId);
    }

    private <T> T getParameter(Map<String, Object> parameters, String key, Class<T> clazz) {
        Object value = parameters.get(key);
        return clazz.cast(value);
    }

    private Map<String, Object> getParameters(JoinPoint joinPoint) {
        Map<String, Object> parameters = new HashMap<>();

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            parameters.put(parameterNames[i], args[i]);
        }

        return parameters;
    }
}
