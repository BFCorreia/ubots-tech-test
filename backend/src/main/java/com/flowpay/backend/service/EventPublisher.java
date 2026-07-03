package com.flowpay.backend.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDashboardUpdate(String eventType) {
        rabbitTemplate.convertAndSend("flowpay.events.exchange", "dashboard.update", eventType);
    }
}
