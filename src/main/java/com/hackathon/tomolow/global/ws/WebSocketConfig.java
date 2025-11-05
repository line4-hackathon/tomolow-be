package com.hackathon.tomolow.global.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 네이티브 WebSocket
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    // SockJS fallback
    registry.addEndpoint("/ws-sockjs").setAllowedOriginPatterns("*").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 구독 prefix (ex. /topic/price, /queue/notice)
    registry.enableSimpleBroker("/topic", "/queue"); // 서버 → 클라이언트로 “발행(broadcast)”되는 채널
    // 발행 prefix (클라가 send 할 때 쓰고 싶으면 /app/~)
    registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 → 서버로 “전송(send)”할 때의 prefix
  }
}
