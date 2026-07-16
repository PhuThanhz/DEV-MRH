package vn.system.app.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;

    public WebSocketConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/v1/ws-endpoint")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://10.0.3.114:3000",
                    "https://10.0.3.114:3000",
                    "https://hrm.vlotustech.vn"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(2)
                .maxPoolSize(8)
                .queueCapacity(500);
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization == null || authorization.isEmpty()) {
                        throw new AccessDeniedException("Missing WebSocket authorization header");
                    }

                    String bearerToken = authorization.get(0);
                    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                        throw new AccessDeniedException("Invalid WebSocket authorization header");
                    }

                    String token = bearerToken.substring(7);
                    try {
                        Jwt jwt = jwtDecoder.decode(token);
                        java.util.Map<String, Object> userToken = jwt.getClaim("user");
                        if (userToken != null && userToken.get("id") != null) {
                            String userId = userToken.get("id").toString();
                            accessor.setUser(new StompPrincipal(userId));
                        } else {
                            throw new AccessDeniedException("WebSocket token does not contain user id");
                        }
                    } catch (AccessDeniedException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new AccessDeniedException("Invalid WebSocket token", e);
                    }
                }
                return message;
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(2)
                .maxPoolSize(8)
                .queueCapacity(500);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024);
        registration.setSendBufferSizeLimit(512 * 1024);
        registration.setSendTimeLimit(10_000);
    }

    // Custom principal that returns userId instead of email
    static class StompPrincipal implements Principal {
        private final String name;

        StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
