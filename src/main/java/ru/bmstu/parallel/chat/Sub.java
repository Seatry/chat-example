package ru.bmstu.parallel.chat;

import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.zeromq.ZMQ;

public class Sub implements Runnable {
    private String subAddr;
    private WebSocketProtocolHandshakeHandler websocketHandler;
    private ZMQ.Socket subSocket;
    private ZMQ.Context context;

    public Sub(WebSocketProtocolHandshakeHandler websocketHandler, String subAddr) {
        this.websocketHandler = websocketHandler;
        this.subAddr = subAddr;
    }

    @Override
    public void run() {
        create();
        while (true) {
            String message = subSocket.recvStr();
            for (WebSocketChannel socket : websocketHandler.getPeerConnections()) {
                System.out.println(socket);
                WebSockets.sendText(message, socket, null);
            }
        }
    }

    private void create() {
        context = ZMQ.context(1);
        subSocket = context.socket(ZMQ.SUB);
        subSocket.connect(subAddr);
        subSocket.subscribe("");
    }
}
