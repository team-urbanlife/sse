package com.wegotoo.sse.exception;

import com.wegotoo.sse.infra.idempotency.IdempotentRepository;
import lombok.Getter;

@Getter
public class IdempotencyException extends RuntimeException {

    private final String messageId;

    public IdempotencyException(String messageId) {
        this.messageId = messageId;
    }

}
