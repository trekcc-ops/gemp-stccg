package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.PlayerStatistic;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStatsRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public PlayerStatsRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            User resourceOwner = getUserIdFromCookiesOrUri(request);

            Map<Object, Object> response = new HashMap<>();


            List<Map<Object, Object>> casualStatistics = new ArrayList<>();
            for (PlayerStatistic statistic : _gameHistoryService.getCasualPlayerStatistics(resourceOwner)) {
                Map<Object, Object> statistics = new HashMap<>();
                statistics.put("deckName", statistic.getDeckName());
                statistics.put("format", statistic.getFormatName());
                statistics.put("wins", statistic.getWins());
                statistics.put("losses", statistic.getLosses());
                casualStatistics.add(statistics);
            }
            response.put("casual", casualStatistics);

            List<Map<Object, Object>> competitiveStatistics = new ArrayList<>();
            for (PlayerStatistic statistic : _gameHistoryService.getCompetitivePlayerStatistics(resourceOwner)) {
                Map<Object, Object> statistics = new HashMap<>();
                statistics.put("deckName", statistic.getDeckName());
                statistics.put("format", statistic.getFormatName());
                statistics.put("wins", statistic.getWins());
                statistics.put("losses", statistic.getLosses());
                casualStatistics.add(statistics);
            }
            response.put("competitive", competitiveStatistics);
            responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(response));
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

}