package com.gempukku.stccg.async.handler;

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
    private final HallServer _hallServer;
    private final ChatServer _chatServer;
    private final GameHistoryService _gameHistoryService;

    public StatusRequestHandler(ServerObjects objects) {
        _hallServer = objects.getHallServer();
        _chatServer = objects.getChatServer();
        _gameHistoryService = objects.getGameHistoryService();
    }

    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {

            var today = ZonedDateTime.now(ZoneOffset.UTC);
            var yesterday = today.minusDays(1);
            var lastWeek = today.minusDays(7);

            String sb = "Tables count: " + _hallServer.getTablesCount() + ", players in hall: " +
                    _chatServer.getChatRoom("Game Hall").getUserIdsInRoom(false).size() +
                    ", games played in last 24 hours: " + _gameHistoryService.getGamesPlayedCount(yesterday, today) +
                    ", active players in last week: " + _gameHistoryService.getActivePlayersCount(lastWeek, today);

            responseWriter.writeHtmlResponse(sb);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}