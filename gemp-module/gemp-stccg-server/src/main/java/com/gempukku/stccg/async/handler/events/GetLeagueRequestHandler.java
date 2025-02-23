package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.competitive.LeagueMatchResult;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

public class GetLeagueRequestHandler implements UriRequestHandlerNew {

    private final String _leagueType;
    GetLeagueRequestHandler(
            @JsonProperty("leagueType")
            String leagueType
    ) {
        _leagueType = leagueType;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        Document doc = createNewDoc();
        LeagueService leagueService = serverObjects.getLeagueService();
        League league = getLeagueByType(leagueService, _leagueType);

        if (league == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        final LeagueData leagueData = league.getLeagueData(serverObjects.getCardBlueprintLibrary(),
                serverObjects.getFormatLibrary(), serverObjects.getSoloDraftDefinitions());
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();

        int end = allSeries.getLast().getEnd();
        int start = allSeries.getFirst().getStart();
        int currentDate = DateUtils.getCurrentDateAsInt();

        Element leagueElem = doc.createElement("league");
        boolean inLeague = leagueService.isPlayerInLeague(league, resourceOwner);

        leagueElem.setAttribute("member", String.valueOf(inLeague));
        leagueElem.setAttribute("joinable", String.valueOf(!inLeague && end >= currentDate));
        leagueElem.setAttribute("draftable",
                String.valueOf(inLeague && leagueData.isSoloDraftLeague() && start <= currentDate));
        leagueElem.setAttribute("type", league.getType());
        leagueElem.setAttribute("name", league.getName());
        leagueElem.setAttribute("cost", String.valueOf(league.getCost()));
        leagueElem.setAttribute("start", String.valueOf(allSeries.getFirst().getStart()));
        leagueElem.setAttribute("end", String.valueOf(end));

        for (LeagueSeriesData series : allSeries) {
            Element seriesElem = doc.createElement("series");
            seriesElem.setAttribute("type", series.getName());
            seriesElem.setAttribute("maxMatches", String.valueOf(series.getMaxMatches()));
            seriesElem.setAttribute("start", String.valueOf(series.getStart()));
            seriesElem.setAttribute("end", String.valueOf(series.getEnd()));
            seriesElem.setAttribute("formatType", series.getFormat().getCode());
            seriesElem.setAttribute("format", series.getFormat().getName());
            seriesElem.setAttribute("collection", series.getCollectionType().getFullName());
            seriesElem.setAttribute("limited", String.valueOf(series.isLimited()));

            Element matchesElem = doc.createElement("matches");
            Collection<LeagueMatchResult> playerMatches =
                    leagueService.getPlayerMatchesInSeries(league, series, resourceOwner.getName());
            for (LeagueMatchResult playerMatch : playerMatches) {
                Element matchElem = doc.createElement("match");
                matchElem.setAttribute("winner", playerMatch.getWinner());
                matchElem.setAttribute("loser", playerMatch.getLoser());
                matchesElem.appendChild(matchElem);
            }
            seriesElem.appendChild(matchesElem);

            final List<PlayerStanding> standings = leagueService.getLeagueSeriesStandings(league, series);
            for (PlayerStanding standing : standings) {
                Element standingElem = doc.createElement("standing");
                setStandingAttributes(standing, standingElem);
                seriesElem.appendChild(standingElem);
            }

            leagueElem.appendChild(seriesElem);
        }

        List<PlayerStanding> leagueStandings = leagueService.getLeagueStandings(league);
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("leagueStanding");
            setStandingAttributes(standing, standingElem);
            leagueElem.appendChild(standingElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

    private League getLeagueByType(LeagueService leagueService, String type) {
        for (League league : leagueService.getActiveLeagues()) {
            if (league.getType().equals(type))
                return league;
        }
        return null;
    }

    private static void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.getPlayerName());
        standingElem.setAttribute("standing", String.valueOf(standing.getStanding()));
        standingElem.setAttribute("points", String.valueOf(standing.getPoints()));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.getGamesPlayed()));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.getOpponentWin()));
    }


}