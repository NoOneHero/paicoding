package com.github.paicoding.forum.web.front.chat.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * v1.1 stomp协议的websocket实现的chatgpt聊天方式
 *
 * @author YiHui
 * @date 2023/6/5
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker // 开启websocket代理
public class WsChatConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 开启一个简单的基于内存的消息代理，前缀是/user的将消息会转发给消息代理 broker
        // 然后再由消息代理，将消息广播给当前连接的客户端
        config.enableSimpleBroker("/chat");

        // 表示配置一个或多个前缀，通过这些前缀过滤出需要被注解方法处理的消息。
        // 例如，前缀为 /app 的 destination 可以通过@MessageMapping注解的方法处理，
        // 而其他 destination （例如 /topic /queue）将被直接交给 broker 处理
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个 /gpt/{id} 的 WebSocket endPoint; 其中 {id} 用于让用户连接终端时都可以有自己的路径
        // 作为 Principal 的标识，以便实现向指定用户发送信息
        // sockjs 可以解决浏览器对 WebSocket 的兼容性问题，
        registry.addEndpoint("/gpt/{id}/{aiType}")
                .setHandshakeHandler(new AuthHandshakeHandler())
                .addInterceptors(new AuthHandshakeInterceptor())
                // 注意下面这个，不要使用 setAllowedOrigins("*")，使用之后有啥问题可以实操验证一下🐕
                .setAllowedOriginPatterns("*")
        ;
    }

    /**
     * 配置接收消息的拦截器
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInInterceptor());
    }

    /**
     * 配置返回消息的拦截器
     *
     * @param registration
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelOutInterceptor());
    }

    @Bean
    public HandshakeHandler handshakeHandler() {
        return new AuthHandshakeHandler();
    }

    @Bean
    public HttpSessionHandshakeInterceptor handshakeInterceptor() {
        return new AuthHandshakeInterceptor();
    }

    @Bean
    public ChannelInterceptor channelInInterceptor() {
        return new AuthInChannelInterceptor();
    }

    @Bean
    public ChannelInterceptor channelOutInterceptor() {
        return new AuthOutChannelInterceptor();
    }
}
