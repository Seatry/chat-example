package ru.bmstu.parallel.chat;

import org.zeromq.ZMQ;

public class PushSocketHandler {
    private ThreadLocal<ZMQ.Socket> myThreadLocal;

    public PushSocketHandler(String addr) {
        myThreadLocal = ThreadLocal.withInitial(() -> {
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket pushsocket = context.socket(ZMQ.PUSH);
            pushsocket.connect(addr);
            return pushsocket;
        });
    }

    ZMQ.Socket get() {
       return  myThreadLocal.get();
    }
}
