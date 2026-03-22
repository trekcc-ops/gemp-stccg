package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.competitive.LeagueMatchResult;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeries;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeague;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public class GetLeagueRequestHandler implements UriRequestHandler {

    private final League _league;
    private final LeagueService _leagueService;

    GetLeagueRequestHandler(
            @JsonProperty("leagueId")
            int leagueId,
            @JacksonInject LeagueService leagueService) throws HttpProcessingException {
        _leagueService = leagueService;
        League foundLeague = null;
        for (League league : leagueService.getActiveLeagues()) {
            if (league.getLeagueId() == leagueId) {
                foundLeague = league;
                break;
            }
        }
        if (foundLeague != null) {
            _league = foundLeague;
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        Document doc = createNewDoc();

        Element leagueElem = doc.createElement("league");
        boolean inLeague = _leagueService.isPlayerInLeague(_league, resourceOwner);
        String joinable = String.valueOf(!inLeague && _league.getEnd().isAfter(ZonedDateTime.now()));
        String draftable = String.valueOf(inLeague && _league instanceof SoloDraftLeague &&
                _league.getStart().isBefore(ZonedDateTime.now()));

                leagueElem.setAttribute("member", String.valueOf(inLeague));
        leagueElem.setAttribute("joinable", joinable);
        leagueElem.setAttribute("draftable", draftable);
        leagueElem.setAttribute("type", String.valueOf(_league.getLeagueId()));
        leagueElem.setAttribute("name", _league.getName());
        leagueElem.setAttribute("cost", String.valueOf(_league.getCost()));
        leagueElem.setAttribute("start", String.valueOf(_league.getStart()));
        leagueElem.setAttribute("end", String.valueOf(_league.getEnd()));

        for (LeagueSeries series : _league.getAllSeries()) {
            Element seriesElem = doc.createElement("series");
            seriesElem.setAttribute("type", series.getName());
            seriesElem.setAttribute("maxMatches", String.valueOf(series.getMaxMatches()));
            seriesElem.setAttribute("start", String.valueOf(series.getStart()));
            seriesElem.setAttribute("end", String.valueOf(series.getEnd()));
            seriesElem.setAttribute("formatType", series.getFormat().getCode());
            seriesElem.setAttribute("format", series.getFormat().getName());
            seriesElem.setAttribute("collection", _league.getCollectionType().getFullName());
            seriesElem.setAttribute("limited", String.valueOf(_league.isLimited()));

            Element matchesElem = doc.createElement("matches");
            Collection<LeagueMatchResult> playerMatches =
                    _leagueService.getPlayerMatchesInSeries(_league, series, resourceOwner.getName());
            for (LeagueMatchResult playerMatch : playerMatches) {
                Element matchElem = doc.createElement("match");
                matchElem.setAttribute("winner", playerMatch.getWinner());
                matchElem.setAttribute("loser", playerMatch.getLoser());
                matchesElem.appendChild(matchElem);
            }
            seriesElem.appendChild(matchesElem);

            final List<PlayerStanding> standings = _leagueService.getLeagueSeriesStandings(_league, series);
            for (PlayerStanding standing : standings) {
                Element standingElem = doc.createElement("standing");
                setStandingAttributes(standing, standingElem);
                seriesElem.appendChild(standingElem);
            }

            leagueElem.appendChild(seriesElem);
        }

        List<PlayerStanding> leagueStandings = _leagueService.getLeagueStandings(_league);
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("leagueStanding");
            setStandingAttributes(standing, standingElem);
            leagueElem.appendChild(standingElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponseWithNoHeaders(doc);
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