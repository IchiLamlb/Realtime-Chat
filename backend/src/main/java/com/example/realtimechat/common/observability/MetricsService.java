package com.example.realtimechat.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry registry;
    private final AtomicInteger activeSessions;
    private final Counter messagesSent;
    private final Counter messagesFailed;
    private final Timer messageDeliveryLatency;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.activeSessions = registry.gauge("websocket.sessions.active", new AtomicInteger(0));
        this.messagesSent = registry.counter("messages.sent");
        this.messagesFailed = registry.counter("messages.failed");
        this.messageDeliveryLatency = registry.timer("message.delivery.latency");
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        if (activeSessions != null) {
            activeSessions.incrementAndGet();
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        if (activeSessions != null) {
            activeSessions.decrementAndGet();
        }
    }

    public void incrementMessagesSent() {
        messagesSent.increment();
    }

    public void incrementMessagesFailed() {
        messagesFailed.increment();
    }

    public void recordMessageDeliveryLatency(long milliseconds) {
        messageDeliveryLatency.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
