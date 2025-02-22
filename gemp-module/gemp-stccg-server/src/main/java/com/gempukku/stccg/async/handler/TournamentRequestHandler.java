package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.List;

public class TournamentRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final TournamentService _tournamentService;
    private final FormatLibrary _formatLibrary;

    public TournamentRequestHandler(ServerObjects objects) {
        super(objects);
        _tournamentService = objects.getTournamentService();
        _formatLibrary = objects.getFormatLibrary();
    }

    @Override
    public final void handleRequest(String uri, GempHttpRequest gempRequest, ResponseWriter responseWriter)
            throws Exception {
        HttpRequest request = gempRequest.getRequest();
        if (uri.startsWith("/") && uri.endsWith("/html") && // this one is buried deep within the client
                uri.contains("/deck/") && request.method() == HttpMethod.GET) {
            getTournamentDeck(uri.substring(1, uri.indexOf("/deck/")),
                    uri.substring(uri.indexOf("/deck/") + 6, uri.lastIndexOf("/html")), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getTournamentInfo(uri.substring(1), responseWriter); // merge into getTournaments
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void getTournamentInfo(String tournamentId, ResponseWriter responseWriter) throws Exception {
        Document doc = createNewDoc();
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        appendTournamentData(doc, doc, tournament, true);
        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

    private void getTournamentDeck(String tournamentId, String playerName, ResponseWriter responseWriter)
            throws Exception {
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        if (tournament.getTournamentStage() != Tournament.Stage.FINISHED)
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403

        CardDeck deck = _tournamentService.getPlayerDeck(tournamentId, playerName, tournament.getFormat());
        if (deck == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        String result = HTMLUtils.getTournamentDeck(deck, playerName, _formatLibrary, _cardBlueprintLibrary);
        responseWriter.writeHtmlResponse(result);
    }


    private void appendTournamentData(Document doc, Node parentNode, Tournament tournament, boolean includeStandings) {
        Element tournamentElem = doc.createElement("tournament");
        tournamentElem.setAttribute("id", tournament.getTournamentId());
        tournamentElem.setAttribute("name", tournament.getTournamentName());
        tournamentElem.setAttribute("format", _formatLibrary.get(tournament.getFormat()).getName());
        tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
        tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
        tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());
        parentNode.appendChild(tournamentElem);

        if (includeStandings) {
            List<PlayerStanding> leagueStandings = tournament.getCurrentStandings();
            for (PlayerStanding standing : leagueStandings) {
                Element standingElem = doc.createElement("tournamentStanding");
                standingElem.setAttribute("player", standing.getPlayerName());
                standingElem.setAttribute("standing", String.valueOf(standing.getStanding()));
                standingElem.setAttribute("points", String.valueOf(standing.getPoints()));
                standingElem.setAttribute("gamesPlayed", String.valueOf(standing.getGamesPlayed()));
                DecimalFormat format = new DecimalFormat("##0.00%");
                standingElem.setAttribute("opponentWin", format.format(standing.getOpponentWin()));
                tournamentElem.appendChild(standingElem);
            }
        }
    }
}