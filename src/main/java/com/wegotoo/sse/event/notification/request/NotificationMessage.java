package com.wegotoo.sse.event.notification.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationMessage {

    private Long id;
    private Long fromId;
    private Long toId;
    private String fromName;
    private String toName;
    private String event;
    private String message;

    @Builder
    private NotificationMessage(Long id, Long fromId, Long toId, String fromName, String toName, String event,
                                String message) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.fromName = fromName;
        this.toName = toName;
        this.event = event;
        this.message = message;
    }

}
