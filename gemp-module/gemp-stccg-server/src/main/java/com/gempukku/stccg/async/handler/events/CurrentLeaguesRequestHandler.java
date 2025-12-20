package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CurrentLeaguesRequestHandler implements UriRequestHandler {

    private final LeagueService _leagueService;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final DraftFormatLibrary _draftLibrary;
    private final FormatLibrary _formatLibrary;

    CurrentLeaguesRequestHandler(@JacksonInject LeagueService leagueService,
                                 @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
                                 @JacksonInject DraftFormatLibrary draftLibrary,
                                 @JacksonInject FormatLibrary formatLibrary) {
        _leagueService = leagueService;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _draftLibrary = draftLibrary;
        _formatLibrary = formatLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        Document doc = createNewDoc();
        Element leagues = doc.createElement("leagues");

        for (League league : _leagueService.getActiveLeagues()) {

            Element leagueElem = doc.createElement("league");
            leagueElem.setAttribute("type", league.getType());
            leagueElem.setAttribute("name", league.getName());
            leagueElem.setAttribute("start", String.valueOf(league.getStart()));
            leagueElem.setAttribute("end", String.valueOf(league.getEnd()));

            leagues.appendChild(leagueElem);
        }

        doc.appendChild(leagues);

        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

}