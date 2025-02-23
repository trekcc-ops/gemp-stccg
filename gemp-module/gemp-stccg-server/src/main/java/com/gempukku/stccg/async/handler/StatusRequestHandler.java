package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class StatusRequestHandler {

    public final void handleRequest(String uri, GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
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
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}