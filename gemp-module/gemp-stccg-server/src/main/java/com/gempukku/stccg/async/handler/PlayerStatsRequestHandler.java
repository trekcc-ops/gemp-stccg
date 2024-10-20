package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.PlayerStatistic;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.List;

public class PlayerStatsRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public PlayerStatsRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            User resourceOwner = getResourceOwner(request);

            List<PlayerStatistic> casualStatistics = _gameHistoryService.getCasualPlayerStatistics(resourceOwner);
            List<PlayerStatistic> competitiveStatistics =
                    _gameHistoryService.getCompetitivePlayerStatistics(resourceOwner);

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
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private static void appendStatistics(Iterable<? extends PlayerStatistic> statistics, Document doc, Node type) {
        for (PlayerStatistic casualStatistic : statistics) {
            Element entry = doc.createElement("entry");
            int wins = casualStatistic.getWins();
            int losses = casualStatistic.getLosses();

            entry.setAttribute("deckName", casualStatistic.getDeckName());
            entry.setAttribute("format", casualStatistic.getFormatName());
            entry.setAttribute("wins", String.valueOf(wins));
            entry.setAttribute("losses", String.valueOf(losses));
            entry.setAttribute("percentage",
                    new DecimalFormat("#0.0%").format(1.0f * wins / (losses + wins)));
            type.appendChild(entry);
        }
    }

}