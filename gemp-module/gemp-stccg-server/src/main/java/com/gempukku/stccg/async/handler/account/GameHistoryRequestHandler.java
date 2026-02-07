package com.gempukku.stccg.async.handler.account;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameHistoryService;

import java.net.HttpURLConnection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class GameHistoryRequestHandler implements UriRequestHandler {

    private final int _start;
    private final int _count;
    private final GameHistoryService _gameHistoryService;
    GameHistoryRequestHandler(
            @JsonProperty(value = "start", required = true)
            int start,
            @JsonProperty(value = "count", required = true)
            int count,
            @JacksonInject GameHistoryService gameHistoryService) {
        _start = start;
        _count = count;
        _gameHistoryService = gameHistoryService;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        if (_start < 0 || _count < 1 || _count > 100)
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        User resourceOwner = request.user();
        String userId = request.userName();

        final List<DBData.GameHistory> playerGameHistory =
                _gameHistoryService.getGameHistoryForPlayer(resourceOwner, _start, _count);
        int recordCount = _gameHistoryService.getGameHistoryForPlayerCount(resourceOwner);

        Map<Object, Object> response = new HashMap<>();
        response.put("count", recordCount);
        response.put("playerId", userId);

        List<Map<Object, Object>> historyEntries = new ArrayList<>();

        for (DBData.GameHistory game : playerGameHistory) {
            Map<Object, Object> historyEntry = new HashMap<>();
            historyEntry.put("winner", game.winner);
            historyEntry.put("loser", game.loser);
            historyEntry.put("winReason", game.win_reason);
            historyEntry.put("loseReason", game.lose_reason);
            historyEntry.put("formatName", game.format_name);
            if (game.tournament != null)
                historyEntry.put("tournament", game.tournament);


            if (game.winner.equals(userId) && game.win_recording_id != null) {
                historyEntry.put("gameRecordingId", game.win_recording_id);
                historyEntry.put("deckName", game.winner_deck_name);
            } else if (game.loser.equals(userId) && game.lose_recording_id != null) {
                historyEntry.put("gameRecordingId", game.lose_recording_id);
                historyEntry.put("deckName", game.loser_deck_name);
            }

            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            historyEntry.put("startTime", game.start_date.format(formatter));
            historyEntry.put("endTime", game.end_date.format(formatter));

            historyEntries.add(historyEntry);
        }
        response.put("games", historyEntries);
        responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(response));
    }

}