package com.gempukku.stccg.async;

import com.gempukku.stccg.async.handler.RootUriRequestHandler;
import com.gempukku.stccg.builder.DaoBuilder;
import com.gempukku.stccg.builder.ServerBuilder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GempServer {

    private static final Logger LOGGER = LogManager.getLogger(GempServer.class);

    public static void main(String[] server) throws InterruptedException {
        int httpPort = Integer.parseInt(AppConfig.getProperty("port"));

        Map<Type, Object> objects = new HashMap<>();

        Thread.sleep(2_000); // sleep for 2 sec to allow time to create database

        //Libraries and other important prereq managers that are used by lots of other managers
        LOGGER.info("GempukkuServer loading prerequisites...");
        ServerBuilder.CreatePrerequisites(objects);
        //Now bulk initialize various managers
        LOGGER.info("GempukkuServer loading DAOs...");
        DaoBuilder.CreateDatabaseAccessObjects(objects);
        LOGGER.info("GempukkuServer loading services...");
        ServerBuilder.CreateServices(objects);
        LOGGER.info("GempukkuServer starting servers...");
        ServerBuilder.StartServers(objects);
        LOGGER.info("GempukkuServer startup complete.");

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            LongPollingSystem longPollingSystem = new LongPollingSystem();
            longPollingSystem.start();

            RootUriRequestHandler uriRequestHandler = new RootUriRequestHandler(objects, longPollingSystem);

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
