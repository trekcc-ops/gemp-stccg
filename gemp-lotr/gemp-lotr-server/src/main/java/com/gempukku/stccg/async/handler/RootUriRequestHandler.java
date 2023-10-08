package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.LongPollingSystem;
import io.netty.handler.codec.http.HttpRequest;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RootUriRequestHandler implements UriRequestHandler {
    final Map<String, UriRequestHandler> requestHandlers = new HashMap<>();
    final String _serverContextPath = "/gemp-lotr-server/";
    final String _webContextPath = "/gemp-lotr/";
    private final WebRequestHandler _webRequestHandler;
    private final StatusRequestHandler _statusRequestHandler;

    private final Pattern originPattern;

    public RootUriRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        _webRequestHandler = new WebRequestHandler();
        _statusRequestHandler = new StatusRequestHandler(context);
        String originAllowedPattern = AppConfig.getProperty("origin.allowed.pattern");
        originPattern = Pattern.compile(originAllowedPattern);

        requestHandlers.put(_serverContextPath + "hall", new HallRequestHandler(context, longPollingSystem));
        requestHandlers.put(_serverContextPath + "deck", new DeckRequestHandler(context));
        requestHandlers.put(_serverContextPath + "login", new LoginRequestHandler(context));
        requestHandlers.put(_serverContextPath + "register", new RegisterRequestHandler(context));
        requestHandlers.put(_serverContextPath + "replay", new ReplayRequestHandler(context));
        requestHandlers.put(_serverContextPath + "gameHistory", new GameHistoryRequestHandler(context));
        requestHandlers.put(_serverContextPath + "stats", new ServerStatsRequestHandler(context));
        requestHandlers.put(_serverContextPath + "playerStats", new PlayerStatsRequestHandler(context));
        requestHandlers.put(_serverContextPath + "admin", new AdminRequestHandler(context));
        requestHandlers.put(_serverContextPath + "chat", new ChatRequestHandler(context, longPollingSystem));
        requestHandlers.put(_serverContextPath + "collection", new CollectionRequestHandler(context));
        requestHandlers.put(_serverContextPath + "delivery", new DeliveryRequestHandler(context));
        requestHandlers.put(_serverContextPath + "game", new GameRequestHandler(context, longPollingSystem));
        requestHandlers.put(_serverContextPath + "league", new LeagueRequestHandler(context));
        requestHandlers.put(_serverContextPath + "merchant", new MerchantRequestHandler(context));
        requestHandlers.put(_serverContextPath + "tournament", new TournamentRequestHandler(context));
        requestHandlers.put(_serverContextPath + "soloDraft", new SoloDraftRequestHandler(context));
        requestHandlers.put(_serverContextPath + "playtesting", new PlaytestRequestHandler(context));
        requestHandlers.put(_serverContextPath + "player", new PlayerInfoRequestHandler(context));
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context,
                              ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.startsWith(_webContextPath)) {
            _webRequestHandler.handleRequest(uri.substring(_webContextPath.length()), request, context, responseWriter, remoteIp);
        } else if (uri.equals("/gemp-lotr")) {
            responseWriter.writeError(301, Collections.singletonMap("Location", "/gemp-lotr/"));
        } else if (uri.equals(_serverContextPath)) {
            _statusRequestHandler.handleRequest(uri.substring(_serverContextPath.length()), request, context, responseWriter, remoteIp);
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
                            uri.substring(entry.getKey().length()), request, context, responseWriter, remoteIp
                    );
                    requestHandled = true;
                }
            }
            if (!requestHandled)
                throw new HttpProcessingException(404);
        }
    }
}
