package com.splat.task.client;

import com.splat.task.client.AccountClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class Client {
    private final static int ADDER_COUNT = 5;
    private final static int READER_COUNT = 5;
    private final static CountDownLatch running_connection = new CountDownLatch(ADDER_COUNT * READER_COUNT);


    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        Logger.getRootLogger().addAppender(new ConsoleAppender());
        Logger.getRootLogger().setLevel(Level.ALL);
        BasicConfigurator.configure();

        URI uri = new URI("http://127.0.0.1:8080/");
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();

        EventLoopGroup group = new NioEventLoopGroup();
        for (int i = 0; i < ADDER_COUNT; i++)
            createClientConnection(group, host, port, ClientConnectionRole.ADDER);
        for (int i = 0; i < READER_COUNT; i++)
            createClientConnection(group, host, port, ClientConnectionRole.READER);
        running_connection.await();
    }

    private static Bootstrap createClientConnection(EventLoopGroup group, String host, int port, ClientConnectionRole role) {
        Bootstrap bootstrap = new Bootstrap();
        AccountClientHandler client = new AccountClientHandler(role, running_connection);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline()
                        .addLast(new HttpClientCodec())
                        .addLast(client);
            }
        });

        bootstrap.connect(host, port).addListener(client);
        return bootstrap;
    }


}
