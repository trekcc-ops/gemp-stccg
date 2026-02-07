package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.hall.HallServer;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class HallStatusRequestHandler implements UriRequestHandler {

    private final GameHistoryService _gameHistoryService;
    private final int _tablesCount;
    private final int _usersInGameHall;

    HallStatusRequestHandler(@JacksonInject HallServer hallServer,
    @JacksonInject ChatServer chatServer,
                             @JacksonInject GameHistoryService gameHistoryService) {
        _tablesCount = hallServer.getTablesCount();
        _usersInGameHall = chatServer.getChatRoom("Game Hall").getUserIdsInRoom(false).size();
        _gameHistoryService = gameHistoryService;
    }

    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        var today = ZonedDateTime.now(ZoneOffset.UTC);
        var yesterday = today.minusDays(1);
        var lastWeek = today.minusDays(7);

        String sb = "Tables count: " + _tablesCount + ", players in hall: " + _usersInGameHall +
                ", games played in last 24 hours: " + _gameHistoryService.getGamesPlayedCount(yesterday, today) +
                ", active players in last week: " + _gameHistoryService.getActivePlayersCount(lastWeek, today);

        responseWriter.writeHtmlResponse(sb);
    }
}