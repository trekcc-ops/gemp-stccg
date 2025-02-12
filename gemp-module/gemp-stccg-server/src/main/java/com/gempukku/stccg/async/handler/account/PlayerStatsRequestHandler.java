package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.PlayerStatistic;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameHistoryService;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class PlayerStatsRequestHandler implements UriRequestHandlerNew {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {

        GameHistoryService gameHistoryService = serverObjects.getGameHistoryService();

            User resourceOwner = request.user();

            Map<Object, Object> response = new HashMap<>();

            List<Map<Object, Object>> casualStatistics = new ArrayList<>();
            for (PlayerStatistic statistic : gameHistoryService.getCasualPlayerStatistics(resourceOwner)) {
                Map<Object, Object> statistics = new HashMap<>();
                statistics.put("deckName", statistic.getDeckName());
                statistics.put("format", statistic.getFormatName());
                statistics.put("wins", statistic.getWins());
                statistics.put("losses", statistic.getLosses());
                casualStatistics.add(statistics);
            }
            response.put("casual", casualStatistics);

            List<Map<Object, Object>> competitiveStatistics = new ArrayList<>();
            for (PlayerStatistic statistic : gameHistoryService.getCompetitivePlayerStatistics(resourceOwner)) {
                Map<Object, Object> statistics = new HashMap<>();
                statistics.put("deckName", statistic.getDeckName());
                statistics.put("format", statistic.getFormatName());
                statistics.put("wins", statistic.getWins());
                statistics.put("losses", statistic.getLosses());
                casualStatistics.add(statistics);
            }
            response.put("competitive", competitiveStatistics);
            responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(response));
    }

}