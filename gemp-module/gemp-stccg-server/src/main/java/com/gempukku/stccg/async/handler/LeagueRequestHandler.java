package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.competitive.PlayerStanding;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.db.vo.LeagueMatchResult;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LeagueRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final FormatLibrary _formatLibrary;
    private final CardBlueprintLibrary _library;

    public LeagueRequestHandler(Map<Type, Object> context) {
        super(context);

        _library = extractObject(context, CardBlueprintLibrary.class);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _formatLibrary = extractObject(context, FormatLibrary.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getNonExpiredLeagues(responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getLeagueInformation(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinLeague(request, uri.substring(1), responseWriter, remoteIp);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void joinLeague(HttpRequest request, String leagueType, ResponseWriter responseWriter, String remoteIp) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        League league = _leagueService.getLeagueByType(leagueType);
        if (league == null)
            throw new HttpProcessingException(404);

        if (!_leagueService.playerJoinsLeague(league, resourceOwner, remoteIp))
            throw new HttpProcessingException(409);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getLeagueInformation(HttpRequest request, String leagueType, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        Document doc = documentBuilder.newDocument();

        League league = getLeagueByType(leagueType);

        if (league == null)
            throw new HttpProcessingException(404);

        final LeagueData leagueData = league.getLeagueData(_library, _formatLibrary, _soloDraftDefinitions);
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();

        int end = allSeries.getLast().getEnd();
        int start = allSeries.getFirst().getStart();
        int currentDate = DateUtils.getCurrentDateAsInt();

        Element leagueElem = doc.createElement("league");
        boolean inLeague = _leagueService.isPlayerInLeague(league, resourceOwner);

        leagueElem.setAttribute("member", String.valueOf(inLeague));
        leagueElem.setAttribute("joinable", String.valueOf(!inLeague && end >= currentDate));
        leagueElem.setAttribute("draftable", String.valueOf(inLeague && leagueData.isSoloDraftLeague() && start <= currentDate));
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
            Collection<LeagueMatchResult> playerMatches = _leagueService.getPlayerMatchesInSerie(league, series, resourceOwner.getName());
            for (LeagueMatchResult playerMatch : playerMatches) {
                Element matchElem = doc.createElement("match");
                matchElem.setAttribute("winner", playerMatch.getWinner());
                matchElem.setAttribute("loser", playerMatch.getLoser());
                matchesElem.appendChild(matchElem);
            }
            seriesElem.appendChild(matchesElem);

            final List<PlayerStanding> standings = _leagueService.getLeagueSerieStandings(league, series);
            for (PlayerStanding standing : standings) {
                Element standingElem = doc.createElement("standing");
                setStandingAttributes(standing, standingElem);
                seriesElem.appendChild(standingElem);
            }

            leagueElem.appendChild(seriesElem);
        }

        List<PlayerStanding> leagueStandings = _leagueService.getLeagueStandings(league);
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("leagueStanding");
            setStandingAttributes(standing, standingElem);
            leagueElem.appendChild(standingElem);
        }

        doc.appendChild(leagueElem);

        responseWriter.writeXmlResponse(doc);
    }

    private void getNonExpiredLeagues(ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element leagues = doc.createElement("leagues");

        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueData leagueData = league.getLeagueData(_library, _formatLibrary, _soloDraftDefinitions);
            final List<LeagueSeriesData> series = leagueData.getSeries();

            int end = series.getLast().getEnd();

            Element leagueElem = doc.createElement("league");

            leagueElem.setAttribute("type", league.getType());
            leagueElem.setAttribute("name", league.getName());
            leagueElem.setAttribute("start", String.valueOf(series.getFirst().getStart()));
            leagueElem.setAttribute("end", String.valueOf(end));

            leagues.appendChild(leagueElem);
        }

        doc.appendChild(leagues);

        responseWriter.writeXmlResponse(doc);
    }

    public League getLeagueByType(String type) {
        for (League league : _leagueService.getActiveLeagues()) {
            if (league.getType().equals(type))
                return league;
        }
        return null;
    }

    private void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.getPlayerName());
        standingElem.setAttribute("standing", String.valueOf(standing.getStanding()));
        standingElem.setAttribute("points", String.valueOf(standing.getPoints()));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.getGamesPlayed()));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.getOpponentWin()));
    }

}
