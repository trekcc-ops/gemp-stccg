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
    private final WebRequestHandler _webRequestHandler;
    private final StatusRequestHandler _statusRequestHandler;
    private final Pattern originPattern;
    private final ServerObjects _serverObjects;
    private final ObjectMapper _jsonMapper = new ObjectMapper();

    public RootUriRequestHandler(ServerObjects objects) {
        _webRequestHandler = new WebRequestHandler();
        _serverObjects = objects;
        _statusRequestHandler = new StatusRequestHandler(objects);
        String originAllowedPattern = AppConfig.getProperty("origin.allowed.pattern");
        originPattern = Pattern.compile(originAllowedPattern);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.contains("?"))
            uri = uri.substring(0, uri.indexOf('?'));

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
            String afterHandlerType = afterServer.substring(handlerType.length());

            Map<String, String> parameters = switch(handlerType) {
                case "cancelGame", "gameCardInfo", "concedeGame", "decisionResponse", "getGameState", "gameHistory",
                        "getChat", "login", "playerInfo", "playerStats", "postChat", "register", "replay",
                        "sendChatMessage", "serverStats", "startGameSession", "updateGameState" -> {
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
                UriRequestHandler handler = switch(handlerType) {
                    case "admin" -> new AdminRequestHandler(_serverObjects);
                    case "collection" -> new CollectionRequestHandler(_serverObjects);
                    case "deck" -> new DeckRequestHandler(_serverObjects);
                    case "hall" -> new HallRequestHandler(_serverObjects);
                    case "league" -> new LeagueRequestHandler(_serverObjects);
                    case "playtesting" -> new PlaytestRequestHandler(_serverObjects);
                    case "soloDraft" -> new SoloDraftRequestHandler(_serverObjects);
                    case "tournament" -> new TournamentRequestHandler(_serverObjects);
                    default -> null;
                };
                if (handler != null) {
                    handler.handleRequest(afterHandlerType, request, responseWriter, remoteIp);
                    requestHandled = true;
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