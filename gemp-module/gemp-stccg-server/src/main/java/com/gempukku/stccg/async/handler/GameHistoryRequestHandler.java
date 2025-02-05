package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.database.GameHistoryDAO;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.HttpURLConnection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHistoryRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public GameHistoryRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getGameHistory(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getGameHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        String startParameter = getQueryParameterSafely(queryDecoder, FormParameter.start);
        String countParameter = getQueryParameterSafely(queryDecoder, FormParameter.count);

        int start = Integer.parseInt(startParameter);
        int count = Integer.parseInt(countParameter);

        if (start < 0 || count < 1 || count > 100)
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        User resourceOwner = getResourceOwnerSafely(request, participantId);
        GameHistoryDAO gameHistoryDAO = _serverObjects.getGameHistoryDAO();

        final List<DBData.GameHistory> playerGameHistory =
                gameHistoryDAO.getGameHistoryForPlayer(resourceOwner, start, count);
        int recordCount = _gameHistoryService.getGameHistoryForPlayerCount(resourceOwner);

        Map<Object, Object> response = new HashMap<>();
        response.put("count", recordCount);
        response.put("playerId", resourceOwner.getName());

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


            if (game.winner.equals(resourceOwner.getName()) && game.win_recording_id != null) {
                historyEntry.put("gameRecordingId", game.win_recording_id);
                historyEntry.put("deckName", game.winner_deck_name);
            } else if (game.loser.equals(resourceOwner.getName()) && game.lose_recording_id != null) {
                historyEntry.put("gameRecordingId", game.lose_recording_id);
                historyEntry.put("deckName", game.loser_deck_name);
            }

            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            historyEntry.put("startTime", game.start_date.format(formatter));
            historyEntry.put("endTime", game.end_date.format(formatter));

            historyEntries.add(historyEntry);
        }
        response.put("games", historyEntries);
        responseWriter.writeJsonResponse(_jsonMapper.writeValueAsString(response));
    }
}