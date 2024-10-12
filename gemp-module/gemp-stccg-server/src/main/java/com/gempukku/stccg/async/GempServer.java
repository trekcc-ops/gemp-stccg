package com.gempukku.stccg.async;

import com.gempukku.stccg.async.handler.RootUriRequestHandler;
import com.gempukku.stccg.common.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class GempServer {

    private final static long DATABASE_CREATION_TIME = 2000;

    public static void main(String[] server) throws InterruptedException {
        int httpPort = Integer.parseInt(AppConfig.getProperty("port"));

        Thread.sleep(DATABASE_CREATION_TIME); // sleep for 2 sec to allow time to create database

        ServerObjects objects = new ServerObjects();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            LongPollingSystem longPollingSystem = new LongPollingSystem();
            longPollingSystem.start();

            RootUriRequestHandler uriRequestHandler = new RootUriRequestHandler(longPollingSystem, objects);

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(Short.MAX_VALUE));
                            pipeline.addLast(new HttpContentCompressor());
                            pipeline.addLast(new GempukkuHttpRequestHandler(objects, uriRequestHandler));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture bind = b.bind(httpPort);
            bind.sync().channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}