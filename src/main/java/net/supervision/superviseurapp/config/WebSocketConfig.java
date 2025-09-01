
// WebSocketConfig.java
package net.supervision.superviseurapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/service-alerts");  // broker simple en mémoire
        config.setApplicationDestinationPrefixes("/app");
    }
    /**
     * Crée un bean RestTemplate qui sera utilisé pour faire des appels HTTP sortants.
     * Spring gérera le cycle de vie de cet objet et l'injectera là où c'est nécessaire (ex: dans TestService).
     * @return une instance de RestTemplate.
     */

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
