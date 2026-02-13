package example.com.server.websocket;

import example.com.server.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;
import java.util.List;

public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public AuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = getFirstHeader(accessor, "Authorization");
            Long userId = jwtService.getUserIdFromToken(authorization);
            if (userId == null) {
                throw new IllegalArgumentException("Unauthorized WebSocket connection");
            }
            accessor.setUser((Principal) userId::toString);
        }
        return message;
    }

    private String getFirstHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }
}
