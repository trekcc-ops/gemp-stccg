package com.gempukku.stccg.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.service.AdminService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ObjectMapper _serverMapper;
    private final AdminService _adminService;

    ServerChannelInitializer(ObjectMapper serverMapper, AdminService adminService) {
        _serverMapper = serverMapper;
        _adminService = adminService;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(Short.MAX_VALUE));
        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new ClientRequestHandler(_serverMapper, _adminService));
    }
}