package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RootUriRequestHandler implements UriRequestHandler {
    private static final String SERVER_CONTEXT_PATH = "/gemp-stccg-server/";
    private final Map<String, UriRequestHandler> requestHandlers = new HashMap<>();
    private final WebRequestHandler _webRequestHandler;
    private final StatusRequestHandler _statusRequestHandler;
    private static final Logger LOGGER = LogManager.getLogger(RootUriRequestHandler.class);
    private final Pattern originPattern;
    private final ServerObjects _serverObjects;
    private final ObjectMapper _jsonMapper = new ObjectMapper();

    public RootUriRequestHandler(LongPollingSystem longPollingSystem, ServerObjects objects) {
        _webRequestHandler = new WebRequestHandler();
        _serverObjects = objects;
        _statusRequestHandler = new StatusRequestHandler(objects);
        String originAllowedPattern = AppConfig.getProperty("origin.allowed.pattern");
        originPattern = Pattern.compile(originAllowedPattern);

        requestHandlers.put(SERVER_CONTEXT_PATH + "admin", new AdminRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "chat", new ChatRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "collection", new CollectionRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "deck", new DeckRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "delivery", new DeliveryRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "game/", new GameRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "hall", new HallRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "league", new LeagueRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "merchant", new MerchantRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "playtesting", new PlaytestRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "replay", new ReplayRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "soloDraft", new SoloDraftRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "stats", new ServerStatsRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "tournament", new TournamentRequestHandler(objects));
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        String webContextPath = "/gemp-module/";
        if (uri.startsWith(webContextPath)) {
            _webRequestHandler.handleRequest(uri.substring(webContextPath.length()), request, responseWriter, remoteIp);
        } else if ("/gemp-module".equals(uri)) {
            // 301 Moved Permanently
            responseWriter.writeError(
                    HttpURLConnection.HTTP_MOVED_PERM, Collections.singletonMap("Location", webContextPath));
        } else if (uri.equals(SERVER_CONTEXT_PATH)) {
            _statusRequestHandler.handleRequest(
                    uri.substring(SERVER_CONTEXT_PATH.length()), request, responseWriter, remoteIp);
        } else {
            String origin = request.headers().get("Origin");
            if (origin != null) {
                if (!originPattern.matcher(origin).matches())
                    throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
            }
            boolean requestHandled = false;

            String afterServer = uri.substring(SERVER_CONTEXT_PATH.length());
            int nextSlashIndex = afterServer.indexOf("/");
            String handlerType = (nextSlashIndex < 0) ? afterServer : afterServer.substring(0, nextSlashIndex);

            Map<String, String> parameters = switch(handlerType) {
                case "gameHistory", "login", "playerInfo", "playerStats", "register" -> {
                    Map<String, String> result = getParameters(request);
                    result.put("type", handlerType);
                    yield result;
                }
                default -> null;
            };

            if (parameters != null) {
                UriRequestHandlerNew handler = _jsonMapper.convertValue(parameters, UriRequestHandlerNew.class);
                handler.handleRequest(uri, request, responseWriter, remoteIp, _serverObjects);
                requestHandled = true;
            } else {

                // These APIs are protected by same Origin protection
                for (Map.Entry<String, UriRequestHandler> entry : requestHandlers.entrySet()) {
                    if (uri.startsWith(entry.getKey())) {
                        entry.getValue().handleRequest(
                                uri.substring(entry.getKey().length()), request, responseWriter, remoteIp
                        );
                        requestHandled = true;
                    }
                }
            }
            if (!requestHandled)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private Map<String, String> getParameters(HttpRequest request) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (request.method() == HttpMethod.POST) {
            InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
                for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                    if (data instanceof Attribute attribute) {
                        result.put(attribute.getName(), attribute.getValue());
                    }
                }
            } finally {
                postDecoder.destroy();
            }
        } else if (request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            for (Map.Entry<String, List<String>> entry : queryDecoder.parameters().entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty() && !entry.getKey().equals("_")) {
                    result.put(entry.getKey(), entry.getValue().getFirst());
                }
            }
        }
        return result;
    }

}