package com.gempukku.stccg.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.service.AdminService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ClientRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger(ClientRequestHandler.class);
    private final ObjectMapper _mapper;
    private final AdminService _adminService;


    public ClientRequestHandler(ObjectMapper serverMapper, AdminService adminService) {
        _adminService = adminService;
        _mapper = serverMapper;
    }

    @Override
    protected final void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest httpRequest) {
        if (HttpUtil.is100ContinueExpected(httpRequest)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
            channelHandlerContext.write(response);
            channelHandlerContext.flush();
        }
        GempHttpRequest request =
                new GempHttpRequest(httpRequest, channelHandlerContext, _adminService);
        request.handle(_mapper);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException) && !(cause instanceof IllegalArgumentException))
            LOGGER.error("Error while processing request", cause);
        ctx.close();
    }


}