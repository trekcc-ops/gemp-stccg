package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.game.GameHistoryService;

import java.util.List;

public class PlaytestReplaysRequestHandler implements UriRequestHandlerNew {

    private final String _format;
    private final int _count;

    public PlaytestReplaysRequestHandler(
            @JsonProperty(value = "format", required = true)
            String format,
            @JsonProperty(value = "count", required = true)
            int count
    ) {
        _format = format;
        _count = count;
    }

    @Override
    public final void handleRequest(GempHttpRequest gempRequest, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        GameHistoryService historyService = serverObjects.getGameHistoryService();
        final List<DBData.GameHistory> gameHistory = historyService.getGameHistoryForFormat(_format, _count);
        String jsonString = new ObjectMapper().writeValueAsString(gameHistory);
        responseWriter.writeJsonResponse(jsonString);
    }

}