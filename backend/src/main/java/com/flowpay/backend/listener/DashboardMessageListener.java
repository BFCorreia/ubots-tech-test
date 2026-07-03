package com.flowpay.backend.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class DashboardMessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardMessageListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "flowpay.dashboard.queue")
    public void receiveMessage(String message) {
        // Converte a mensagem AMQP para STOMP (WebSocket)
        messagingTemplate.convertAndSend("/topic/dashboard", message);
    }
}
