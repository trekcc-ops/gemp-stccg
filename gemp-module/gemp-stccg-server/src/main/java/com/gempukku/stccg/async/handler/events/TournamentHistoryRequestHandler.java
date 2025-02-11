package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.text.DecimalFormat;
import java.util.List;

@JsonIgnoreProperties("participantId")
public class TournamentHistoryRequestHandler implements UriRequestHandlerNew {

    private final static long SEVEN_DAYS_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        long sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_IN_MILLIS;
        getTournamentsData(responseWriter, serverObjects.getTournamentService().getOldTournaments(sevenDaysAgo),
                serverObjects.getFormatLibrary());
    }

    private void getTournamentsData(ResponseWriter responseWriter, Iterable<? extends Tournament> tournamentList,
                                    FormatLibrary formatLibrary)
            throws Exception {
        Document doc = createNewDoc();
        Element tournaments = doc.createElement("tournaments");
        for (Tournament tournament : tournamentList)
            appendTournamentData(doc, tournaments, tournament, false, formatLibrary);
        doc.appendChild(tournaments);
        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

    static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }



    private void appendTournamentData(Document doc, Node parentNode, Tournament tournament, boolean includeStandings,
                                      FormatLibrary formatLibrary) {
        Element tournamentElem = doc.createElement("tournament");
        tournamentElem.setAttribute("id", tournament.getTournamentId());
        tournamentElem.setAttribute("name", tournament.getTournamentName());
        tournamentElem.setAttribute("format", formatLibrary.get(tournament.getFormat()).getName());
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