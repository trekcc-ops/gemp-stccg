package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.hall.HallServer;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class HallStatusRequestHandler implements UriRequestHandler {

    // TODO - This has not been tested since the client calling method doesn't seem to be active

    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        HallServer hallServer = serverObjects.getHallServer();
        ChatServer chatServer = serverObjects.getChatServer();
        GameHistoryService gameHistoryService = serverObjects.getGameHistoryService();

        var today = ZonedDateTime.now(ZoneOffset.UTC);
        var yesterday = today.minusDays(1);
        var lastWeek = today.minusDays(7);

        String sb = "Tables count: " + hallServer.getTablesCount() + ", players in hall: " +
                chatServer.getChatRoom("Game Hall").getUserIdsInRoom(false).size() +
                ", games played in last 24 hours: " + gameHistoryService.getGamesPlayedCount(yesterday, today) +
                ", active players in last week: " + gameHistoryService.getActivePlayersCount(lastWeek, today);

        responseWriter.writeHtmlResponse(sb);
    }
}