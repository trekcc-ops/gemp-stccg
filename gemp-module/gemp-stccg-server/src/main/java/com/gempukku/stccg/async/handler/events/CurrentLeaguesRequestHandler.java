package com.gempukku.stccg.async.handler.events;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class CurrentLeaguesRequestHandler implements UriRequestHandler {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        Document doc = createNewDoc();
        Element leagues = doc.createElement("leagues");
        LeagueService leagueService = serverObjects.getLeagueService();

        for (League league : leagueService.getActiveLeagues()) {

            LeagueData leagueData = league.getLeagueData(serverObjects.getCardBlueprintLibrary(),
                    serverObjects.getFormatLibrary(), serverObjects.getSoloDraftDefinitions());
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

        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

}