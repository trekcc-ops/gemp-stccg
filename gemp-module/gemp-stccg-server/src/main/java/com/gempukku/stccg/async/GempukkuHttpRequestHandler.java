package com.gempukku.stccg.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.gempukku.stccg.async.handler.*;
import com.gempukku.stccg.common.AppConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class GempukkuHttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger(GempukkuHttpRequestHandler.class);
    private final ServerObjects _serverObjects;
    private static final String SERVER_CONTEXT_PATH = "/gemp-stccg-server/";
    private static final String WEB_CONTEXT_PATH = "/gemp-module/";
    private final WebRequestHandler _webRequestHandler;
    private final Pattern originPattern = Pattern.compile(AppConfig.getProperty("origin.allowed.pattern"));
    private final ObjectMapper _jsonMapper = new ObjectMapper();


    public GempukkuHttpRequestHandler(ServerObjects serverObjects) {
        _serverObjects = serverObjects;
        _webRequestHandler = new WebRequestHandler();
    }

    @Override
    protected final void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest httpRequest) {
        if (HttpUtil.is100ContinueExpected(httpRequest)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
            channelHandlerContext.write(response);
            channelHandlerContext.flush();
        }

        GempHttpRequest request = new GempHttpRequest(httpRequest, channelHandlerContext, _serverObjects);
        ResponseWriter responseSender = new ResponseSender(channelHandlerContext, httpRequest);
        handleRequest(request, responseSender);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException) && !(cause instanceof IllegalArgumentException))
            LOGGER.error("Error while processing request", cause);
        ctx.close();
    }


    private void handleRequest(GempHttpRequest request, ResponseWriter responseWriter) {
        try {
            if (request.bannedIp()) {
                responseWriter.writeError(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
                LOGGER.info("Denying entry to user from banned IP {}", request.ip());
            } else {
                String uri = request.uriWithoutParameters();

                if (uri.startsWith(WEB_CONTEXT_PATH)) {
                    _webRequestHandler.handleRequest(uri.substring(WEB_CONTEXT_PATH.length()), request, responseWriter);
                } else if (uri.replace("/", "").isEmpty() ||
                        uri.replace("/", "").equals(WEB_CONTEXT_PATH.replace("/", ""))) {
                    // 301 Moved Permanently
                    responseWriter.writeError(
                            HttpURLConnection.HTTP_MOVED_PERM, Collections.singletonMap("Location", WEB_CONTEXT_PATH));
                } else if (uri.equals(SERVER_CONTEXT_PATH)) {
                    new StatusRequestHandler().handleRequest(request, responseWriter, _serverObjects);
                } else if (uri.startsWith(SERVER_CONTEXT_PATH)) {
                    validateOrigin(request);

                    String afterServer = uri.substring(SERVER_CONTEXT_PATH.length());
                    int nextSlashIndex = afterServer.indexOf("/");
                    String handlerType = (nextSlashIndex < 0) ? afterServer : afterServer.substring(0, nextSlashIndex);

                    if (handlerType.equals("collection")) {
                        String afterHandlerType = afterServer.substring(handlerType.length());
                        new CollectionRequestHandler().handleRequest(afterHandlerType, request, responseWriter, _serverObjects);
                    } else {
                        Map<String, Object> parameters = request.parameters();
                        parameters.put("type", handlerType);
                        try {
                            UriRequestHandler newHandler = _jsonMapper.convertValue(parameters, UriRequestHandler.class);
                            newHandler.handleRequest(request, responseWriter, _serverObjects);
                        } catch (IllegalArgumentException exp) {
                            if (exp.getCause() instanceof InvalidTypeIdException) {
                                // InvalidTypeIdException thrown if initial path not recognized by the Json deserializer
                                logHttpError(uri,
                                        new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND), responseWriter);
                            }
                        } catch (JsonProcessingException exp) {
                            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
                        }
                    }
                }
            }
        } catch (HttpProcessingException exp) {
            logHttpError(request.uri(), exp, responseWriter);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }


    private static void logHttpError(String uri, HttpProcessingException exp, ResponseWriter writer) {
        int code = exp.getStatus();
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code < 500 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            GempukkuHttpRequestHandler.LOGGER.debug("HTTP {} response for {}", code, uri);

            // record an HTTP 400 or 500 error
        else if((code < 600))
            GempukkuHttpRequestHandler.LOGGER.error("HTTP code {} response for {}", code, uri, exp);
        writer.writeError(exp.getStatus());
    }




    private void validateOrigin(GempHttpRequest request) throws HttpProcessingException {
        String origin = request.headers().get("Origin");
        if (origin != null) {
            if (!originPattern.matcher(origin).matches())
                throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }
    }

}