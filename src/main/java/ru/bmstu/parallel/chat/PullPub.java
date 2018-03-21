package ru.bmstu.parallel.chat;

import org.zeromq.ZMQ;

public class PullPub implements Runnable{
    private ZMQ.Socket pullSocket, pubSocket;
    private ZMQ.Context context;
    private String pullAddr;
    private String pubAddr;

    public PullPub(String pullAddr, String pubAddr) {
        this.pullAddr = pullAddr;
        this.pubAddr = pubAddr;
    }

    @Override
    public void run() {
        create();
        ZMQ.proxy(pullSocket, pubSocket, null);
        close();
    }

    private void create() {
        context = ZMQ.context(1);
        pullSocket = context.socket(ZMQ.PULL);
        pullSocket.bind(pullAddr);
        pubSocket = context.socket(ZMQ.PUB);
        pubSocket.connect(pubAddr);
    }

    private void close() {
        pullSocket.close();
        pubSocket.close();
        context.term();
    }
}
