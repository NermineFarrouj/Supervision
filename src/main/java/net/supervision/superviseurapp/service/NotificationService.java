// NotificationService.java
package net.supervision.superviseurapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyServiceDown(String serviceName) {
        String alertMsg = "🚨 Service Alert: " + serviceName + " is DOWN";
        messagingTemplate.convertAndSend("/topic/service-alerts", alertMsg);
    }
}
