package com.example.realtimechat.common.error;

import org.springframework.http.HttpStatus;

public class KafkaPublishException extends BusinessException {
    public KafkaPublishException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "KAFKA_PUBLISH_ERROR", message);
    }
    
    public KafkaPublishException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "KAFKA_PUBLISH_ERROR", message);
        this.initCause(cause);
    }
}
