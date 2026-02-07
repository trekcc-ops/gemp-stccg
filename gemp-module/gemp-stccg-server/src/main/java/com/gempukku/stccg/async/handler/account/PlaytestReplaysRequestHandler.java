package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.game.GameHistoryService;

import java.util.List;

public class PlaytestReplaysRequestHandler implements UriRequestHandler {

    private final List<DBData.GameHistory> _gameHistory;

    public PlaytestReplaysRequestHandler(
            @JsonProperty(value = "format", required = true)
            String format,
            @JsonProperty(value = "count", required = true)
            int count,
            @JacksonInject GameHistoryService gameHistoryService
    ) {
        _gameHistory = gameHistoryService.getGameHistoryForFormat(format, count);
    }

    @Override
    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter)
            throws Exception {
        String jsonString = new ObjectMapper().writeValueAsString(_gameHistory);
        responseWriter.writeJsonResponse(jsonString);
    }

}