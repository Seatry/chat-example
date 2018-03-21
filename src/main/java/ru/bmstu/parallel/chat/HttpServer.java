package ru.bmstu.parallel.chat;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import org.zeromq.ZMQ;


public class HttpServer {
    static final String PUB_XSUB_ADDR = ProxyAddres.xsubAddr;
    static final String SUB_XPUB_ADDR = ProxyAddres.xpubAddr;
    public static void main(final String[] args) {
        final String LOCAL_PORT = args[0];
        final String PUSH_PULL_ADDR = "tcp://localhost:" + args[1];
        PushSocketHandler pushSocketHandler = new PushSocketHandler(PUSH_PULL_ADDR);
        WebSocketProtocolHandshakeHandler websocketHandler = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                    final String messageData = message.getData();
                    ZMQ.Socket pushSocket = pushSocketHandler.get();
                    pushSocket.send(messageData);
                }
            });
            channel.resumeReceives();
        });

        ResourceHandler resourceHandler = Handlers.resource(new ClassPathResourceManager(HttpServer.class.getClassLoader(), ""))
                .addWelcomeFiles("index.html");

        Undertow server = Undertow.builder()
                .addHttpListener(Integer.parseInt(LOCAL_PORT), "localhost") // args вынести в константу
                .setHandler(Handlers.path()
                        .addPrefixPath("/chatsocket", websocketHandler)
                        .addPrefixPath("/", resourceHandler)
                        .addPrefixPath("/connections", Handlers.path(exchange -> {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                            exchange.getResponseSender().send("" + websocketHandler.getPeerConnections().size());
                        }))
                )
                .build();
        server.start();
        new Thread(new PullPub(PUSH_PULL_ADDR, PUB_XSUB_ADDR)).start();
        new Thread(new Sub(websocketHandler, SUB_XPUB_ADDR)).start();
    }
}
