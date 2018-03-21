package ru.bmstu.parallel.chat;

import org.zeromq.ZMQ;

public class CentralProxy {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket frontend = context.socket(ZMQ.XSUB);
        frontend.bind(ProxyAddres.xsubAddr);
        ZMQ.Socket backend = context.socket(ZMQ.XPUB);
        backend.bind(ProxyAddres.xpubAddr);
        ZMQ.proxy(frontend, backend, null);
        frontend.close();
        backend.close();
        context.term();

    }
}
