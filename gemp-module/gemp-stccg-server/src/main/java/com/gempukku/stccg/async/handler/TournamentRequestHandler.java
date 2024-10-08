package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.SortAndFilterCards;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class TournamentRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final TournamentService _tournamentService;
    private final FormatLibrary _formatLibrary;
    private final SortAndFilterCards _sortAndFilterCards;

    public TournamentRequestHandler(Map<Type, Object> context) {
        super(context);

        _tournamentService = extractObject(context, TournamentService.class);
        _formatLibrary = extractObject(context, FormatLibrary.class);
        _sortAndFilterCards = new SortAndFilterCards();
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getCurrentTournaments(responseWriter);
        } else if (uri.equals("/history") && request.method() == HttpMethod.GET) {
            getTournamentHistory(responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/html") && uri.contains("/deck/") && request.method() == HttpMethod.GET) {
            getTournamentDeck(uri.substring(1, uri.indexOf("/deck/")), uri.substring(uri.indexOf("/deck/") + 6, uri.lastIndexOf("/html")), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getTournamentInfo(uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getTournamentInfo(String tournamentId, ResponseWriter responseWriter) throws Exception {
        Document doc = createNewDoc();
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);
        appendTournamentData(doc, doc, tournament, true);
        responseWriter.writeXmlResponse(doc);
    }

    private void getTournamentDeck(String tournamentId, String playerName, ResponseWriter responseWriter)
            throws Exception {
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        if (tournament.getTournamentStage() != Tournament.Stage.FINISHED)
            throw new HttpProcessingException(403);

        CardDeck deck = _tournamentService.getPlayerDeck(tournamentId, playerName, tournament.getFormat());
        if (deck == null)
            throw new HttpProcessingException(404);

        String result = "<html><body>" +
                "<h1>" + StringEscapeUtils.escapeHtml(deck.getDeckName()) + "</h1>" +
                "<h2>by " + playerName + "</h2>" +
                getHTMLDeck(deck, false, _sortAndFilterCards, _formatLibrary) +
                "</body></html>";
        responseWriter.writeHtmlResponse(result);
    }

    private void getTournamentHistory(ResponseWriter responseWriter) throws Exception {
        long sevenDaysAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7);
        getTournamentsData(responseWriter, _tournamentService.getOldTournaments(sevenDaysAgo));
    }

    private void getCurrentTournaments(ResponseWriter responseWriter) throws Exception {
        getTournamentsData(responseWriter, _tournamentService.getLiveTournaments());
    }

    private void getTournamentsData(ResponseWriter responseWriter, List<Tournament> tournamentList) throws Exception {
        Document doc = createNewDoc();
        Element tournaments = doc.createElement("tournaments");
        for (Tournament tournament : tournamentList)
            appendTournamentData(doc, tournaments, tournament, false);
        doc.appendChild(tournaments);
        responseWriter.writeXmlResponse(doc);
    }


    private void appendTournamentData(Document doc, Node parentNode, Tournament tournament, boolean includeStandings) {
        Element tournamentElem = doc.createElement("tournament");
        tournamentElem.setAttribute("id", tournament.getTournamentId());
        tournamentElem.setAttribute("name", tournament.getTournamentName());
        tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormat()).getName());
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
