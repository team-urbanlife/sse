package com.wegotoo.sse.infra.utils;

public class SseMessagePath {

    public static final String MAIN_EXCHANGE_NAME = "app.sse";
    public static final String MAIN_QUEUE_NAME = "app.sse.queue";
    public static final String MAIN_ROUTING_KEY = "sse";
    public static final String RETRY_QUEUE_NAME = "app.sse.queue.retry";
    public static final String RETRY_ROUTING_KEY = "sse.retry";
    public static final String DLX_NAME = "app.sse.error";
    public static final String DLQ_NAME = "app.sse.queue.dead";
    public static final String DLQ_ROUTING_KEY = "sse.dead";

}
