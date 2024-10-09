package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.db.PlayerStatistic;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.game.GameHistoryService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class PlayerStatsRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final GameHistoryService _gameHistoryService;

    public PlayerStatsRequestHandler(Map<Type, Object> context) {
        super(context);
        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            User resourceOwner = getResourceOwner(request);

            List<PlayerStatistic> casualStatistics = _gameHistoryService.getCasualPlayerStatistics(resourceOwner);
            List<PlayerStatistic> competitiveStatistics = _gameHistoryService.getCompetitivePlayerStatistics(resourceOwner);

            Document doc = createNewDoc();
            Element stats = doc.createElement("playerStats");

            Element casual = doc.createElement("casual");
            appendStatistics(casualStatistics, doc, casual);
            stats.appendChild(casual);

            Element competitive = doc.createElement("competitive");
            appendStatistics(competitiveStatistics, doc, competitive);
            stats.appendChild(competitive);

            doc.appendChild(stats);

            responseWriter.writeXmlResponse(doc);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void appendStatistics(List<PlayerStatistic> statistics, Document doc, Element type) {
        for (PlayerStatistic casualStatistic : statistics) {
            Element entry = doc.createElement("entry");
            entry.setAttribute("deckName", casualStatistic.getDeckName());
            entry.setAttribute("format", casualStatistic.getFormatName());
            entry.setAttribute("wins", String.valueOf(casualStatistic.getWins()));
            entry.setAttribute("losses", String.valueOf(casualStatistic.getLosses()));
            entry.setAttribute("percentage", new DecimalFormat("#0.0%").format(
                    1f * casualStatistic.getWins() / (casualStatistic.getLosses() + casualStatistic.getWins())));
            type.appendChild(entry);
        }
    }

}