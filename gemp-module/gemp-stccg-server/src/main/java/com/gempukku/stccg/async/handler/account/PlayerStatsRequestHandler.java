package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.PlayerStatistic;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameHistoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class PlayerStatsRequestHandler implements UriRequestHandler {

    private final GameHistoryService _gameHistoryService;

    PlayerStatsRequestHandler(@JacksonInject GameHistoryService gameHistoryService) {
        _gameHistoryService = gameHistoryService;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        Map<String, List<PlayerStatistic>> response = new HashMap<>();
        response.put("casual", _gameHistoryService.getCasualPlayerStatistics(resourceOwner));
        response.put("competitive", _gameHistoryService.getCompetitivePlayerStatistics(resourceOwner));
        responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(response));
    }

}