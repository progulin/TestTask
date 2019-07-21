package com.splat.task.client;

import com.splat.task.server.AccountServer;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class AccountClientHandler extends SimpleChannelInboundHandler<HttpObject> implements ChannelFutureListener {
    private final static Logger log = Logger.getLogger(AccountServer.class);
    private final Random random = new Random(System.currentTimeMillis());
    private final ClientConnectionRole role;
    private final CountDownLatch running_connection;

    AccountClientHandler(ClientConnectionRole role, CountDownLatch running_connection) {
        super();
        this.role = role;
        this.running_connection = running_connection;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            if (!response.decoderResult().isSuccess()) {
                ctx.close();
                return;
            }
            sendRequest(ctx.channel());
        }
    }

    public void sendRequest(Channel channel) {
        HttpRequest request;
        if ( role == ClientConnectionRole.ADDER ) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/service/add/" + getRandomId() + "/" + getRandomValue());
        } else {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/service/amount/" + getRandomId());
        }
        request.headers().set(HttpHeaderNames.HOST, "localhost");
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        channel.writeAndFlush(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client socket exception", cause);
        ctx.close();
        running_connection.countDown();
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            future.channel().close();
            running_connection.countDown();
        } else {
            sendRequest(future.channel());
        }
    }

    private long getRandomValue() {
        return random.nextInt(1000);
    }

    private long getRandomId() {
        return random.nextInt(20);
    }

}
