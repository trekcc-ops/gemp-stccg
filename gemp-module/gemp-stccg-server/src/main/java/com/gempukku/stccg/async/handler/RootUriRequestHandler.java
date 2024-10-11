package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RootUriRequestHandler implements UriRequestHandler {
    final Map<String, UriRequestHandler> requestHandlers = new HashMap<>();
    final String _serverContextPath = "/gemp-stccg-server/";
    final String _webContextPath = "/gemp-module/";
    private final WebRequestHandler _webRequestHandler;
    private final StatusRequestHandler _statusRequestHandler;

    private final Pattern originPattern;

    public RootUriRequestHandler(LongPollingSystem longPollingSystem, ServerObjects objects) {
        _webRequestHandler = new WebRequestHandler();
        _statusRequestHandler = new StatusRequestHandler(objects);
        String originAllowedPattern = AppConfig.getProperty("origin.allowed.pattern");
        originPattern = Pattern.compile(originAllowedPattern);

        requestHandlers.put(_serverContextPath + "hall", new HallRequestHandler(objects, longPollingSystem));
        requestHandlers.put(_serverContextPath + "deck", new DeckRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "login", new LoginRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "register", new RegisterRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "replay", new ReplayRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "gameHistory", new GameHistoryRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "stats", new ServerStatsRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "playerStats", new PlayerStatsRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "admin", new AdminRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "chat", new ChatRequestHandler(objects, longPollingSystem));
        requestHandlers.put(_serverContextPath + "collection", new CollectionRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "delivery", new DeliveryRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "game", new GameRequestHandler(objects, longPollingSystem));
        requestHandlers.put(_serverContextPath + "league", new LeagueRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "merchant", new MerchantRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "tournament", new TournamentRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "soloDraft", new SoloDraftRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "playtesting", new PlaytestRequestHandler(objects));
        requestHandlers.put(_serverContextPath + "player", new PlayerInfoRequestHandler(objects));
    }

    @Override
    public void handleRequest(String uri, HttpRequest request,
                              ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith(_webContextPath)) {
            _webRequestHandler.handleRequest(uri.substring(_webContextPath.length()), request, responseWriter, remoteIp);
        } else if (uri.equals("/gemp-module")) {
            responseWriter.writeError(301, Collections.singletonMap("Location", _webContextPath));
        } else if (uri.equals(_serverContextPath)) {
            _statusRequestHandler.handleRequest(uri.substring(_serverContextPath.length()), request, responseWriter, remoteIp);
        } else {
            String origin = request.headers().get("Origin");
            if (origin != null) {
                if (!originPattern.matcher(origin).matches())
                    throw new HttpProcessingException(403);
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
                throw new HttpProcessingException(404);
        }
    }

}