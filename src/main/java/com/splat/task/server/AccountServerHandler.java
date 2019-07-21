package com.splat.task.server;

import com.splat.task.AccountServiceImpl;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

public class AccountServerHandler extends SimpleChannelInboundHandler<Object> {
    private final Logger log = Logger.getLogger(AccountServerHandler.class);
    private final static byte[] NEW_LINE = System.lineSeparator().getBytes();
    private final AccountServiceImpl service;

    public AccountServerHandler(AccountServiceImpl service) {
        this.service = service;
    }

    protected void channelRead0(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) message;

            if (!request.decoderResult().isSuccess()) {
                context.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            QueryStringDecoder query = new QueryStringDecoder(request.uri());

            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            try {
                if (query.path().startsWith("/service/")) {
                    processServiceQuery(query, response);
                } else if (query.path().startsWith("/stat/")) {
                    processStatQuery(query, response);
                } else {
                    response.setStatus(HttpResponseStatus.NOT_FOUND);
                }
            } catch (Exception ex) {
                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                response.content().writeBytes(ex.toString().getBytes());
            } finally {
                if (HttpUtil.isKeepAlive(request)) {
                    response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }
                context.writeAndFlush(response);
            }
        }

    }

    private void processStatQuery(QueryStringDecoder query, FullHttpResponse response) {
        if ("/stat/rate".equals(query.path())) {
            response.content().writeBytes(Long.toString(service.getStatistic().getRate(System.currentTimeMillis())).getBytes()).writeBytes(NEW_LINE);
            return;
        }
        if ("/stat/count".equals(query.path())) {
            response.content().writeBytes(Long.toString(service.getStatistic().getCounter()).getBytes()).writeBytes(NEW_LINE);
            return;
        }
        if ("/stat/clear".equals(query.path())) {
            service.getStatistic().clear();
            return;
        }
    }

    private void processServiceQuery(QueryStringDecoder query, FullHttpResponse response) {
        String args[] = query.path().split("/", 5);

        if ("amount".equals(args[2]) && args.length == 4) {
            int id = Integer.parseInt(args[3]);
            long val = service.getAmount(id);
            response.content().writeBytes(Long.toString(val).getBytes()).writeBytes(NEW_LINE);
            return;
        }

        if ("add".equals(args[2]) && args.length == 5) {
            int id = Integer.parseInt(args[3]);
            long val = Long.parseLong(args[4]);
            service.addAmount(id, val);
            return;
        }

        response.setStatus(HttpResponseStatus.BAD_REQUEST);
        log.warn("Query: " + query.path());
    }
}
