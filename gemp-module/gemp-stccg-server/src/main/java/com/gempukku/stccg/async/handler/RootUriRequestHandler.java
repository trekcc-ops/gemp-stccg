package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RootUriRequestHandler implements UriRequestHandler {
    private static final String SERVER_CONTEXT_PATH = "/gemp-stccg-server/";
    private final Map<String, UriRequestHandler> requestHandlers = new HashMap<>();
    private final WebRequestHandler _webRequestHandler;
    private final StatusRequestHandler _statusRequestHandler;

    private final Pattern originPattern;

    public RootUriRequestHandler(LongPollingSystem longPollingSystem, ServerObjects objects) {
        _webRequestHandler = new WebRequestHandler();
        _statusRequestHandler = new StatusRequestHandler(objects);
        String originAllowedPattern = AppConfig.getProperty("origin.allowed.pattern");
        originPattern = Pattern.compile(originAllowedPattern);

        requestHandlers.put(SERVER_CONTEXT_PATH + "admin", new AdminRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "chat", new ChatRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "collection", new CollectionRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "deck", new DeckRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "delivery", new DeliveryRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "game/", new GameRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "gameHistory", new GameHistoryRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "hall", new HallRequestHandler(objects, longPollingSystem));
        requestHandlers.put(SERVER_CONTEXT_PATH + "league", new LeagueRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "login", new LoginRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "merchant", new MerchantRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "playerInfo", new PlayerInfoRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "playerStats", new PlayerStatsRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "playtesting", new PlaytestRequestHandler(objects));
        requestHandlers.put(SERVER_CONTEXT_PATH + "register", new RegisterRequestHandler(objects));
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

            // These APIs are protected by same Origin protection
            for (Map.Entry<String, UriRequestHandler> entry : requestHandlers.entrySet()) {
                if (uri.startsWith(entry.getKey())) {
                    entry.getValue().handleRequest(
                            uri.substring(entry.getKey().length()), request, responseWriter, remoteIp
                    );
                    requestHandled = true;
                }
            }
            if (!requestHandled)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

}