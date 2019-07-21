package com.splat.task.server;

import com.splat.task.AccountServiceImpl;
import com.splat.task.commons.Settings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;


public class AccountServer extends ChannelInitializer<SocketChannel> {
    private final static Logger log = Logger.getLogger(AccountServer.class);

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServerBootstrap bootstrap;
    private final AccountServiceImpl service;


    public AccountServer(AccountServiceImpl service) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        this.service = service;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new AccountServerHandler(service));
    }

    public void start() {
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(this);
            Channel ch = bootstrap.bind(Settings.getInstance().WEB_SERVER_PORT.value()).sync().channel();
            workerGroup.scheduleAtFixedRate(this::printStatistic, 1,1, TimeUnit.SECONDS);
            ch.closeFuture().sync();
        } catch (Exception ex) {
            log.error("Interrupt server", ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void printStatistic() {
        log.info("Rate: " + service.getStatistic().getRate(System.currentTimeMillis()));
        log.info("Count: " + service.getStatistic().getCounter());
    }
}
