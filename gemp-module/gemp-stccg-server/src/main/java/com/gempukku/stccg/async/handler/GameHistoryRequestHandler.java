package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DBData;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.db.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GameHistoryRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public GameHistoryRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getGameHistory(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getGameHistory(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        String startParameter = getQueryParameterSafely(queryDecoder, "start");
        String countParameter = getQueryParameterSafely(queryDecoder, "count");

        int start = Integer.parseInt(startParameter);
        int count = Integer.parseInt(countParameter);

        if (start < 0 || count < 1 || count > 100) throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        final List<DBData.GameHistory> playerGameHistory =
                _gameHistoryService.getGameHistoryForPlayer(resourceOwner, start, count);
        int recordCount = _gameHistoryService.getGameHistoryForPlayerCount(resourceOwner);

        Document doc = createNewDoc();
        Element gameHistory = doc.createElement("gameHistory");
        gameHistory.setAttribute("count", String.valueOf(recordCount));
        gameHistory.setAttribute("playerId", resourceOwner.getName());

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (DBData.GameHistory game : playerGameHistory) {
            Element historyEntry = doc.createElement("historyEntry");
            historyEntry.setAttribute("winner", game.winner);
            historyEntry.setAttribute("loser", game.loser);

            historyEntry.setAttribute("winReason", game.win_reason);
            historyEntry.setAttribute("loseReason", game.lose_reason);

            historyEntry.setAttribute("formatName", game.format_name);
            String tournament = game.tournament;
            if (tournament != null)
                historyEntry.setAttribute("tournament", tournament);

            if (game.winner.equals(resourceOwner.getName()) && game.win_recording_id != null) {
                historyEntry.setAttribute("gameRecordingId", game.win_recording_id);
                historyEntry.setAttribute("deckName", game.winner_deck_name);
            } else if (game.loser.equals(resourceOwner.getName()) && game.lose_recording_id != null) {
                historyEntry.setAttribute("gameRecordingId", game.lose_recording_id);
                historyEntry.setAttribute("deckName", game.loser_deck_name);
            }

            historyEntry.setAttribute("startTime", game.start_date.format(formatter));
            historyEntry.setAttribute("endTime", game.end_date.format(formatter));

            gameHistory.appendChild(historyEntry);
        }

        doc.appendChild(gameHistory);

        responseWriter.writeXmlResponse(doc);
    }
}