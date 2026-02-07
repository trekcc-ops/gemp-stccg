package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SoloDraftLeague;
import org.w3c.dom.Document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LeagueAdminDraftRequestHandler implements AdminRequestHandler, NewLeagueHandler {

    private final boolean _preview;
    private final LeagueService _leagueService;
    private final SoloDraftLeague _league;

    LeagueAdminDraftRequestHandler(
            @JsonProperty(value = "start", required = true)
        String start,
            @JsonProperty(value = "name", required = true)
        String name,
            @JsonProperty(value = "draftType", required = true)
        String draftType,
            @JsonProperty(value = "seriesDuration", required = true)
        int seriesDuration,
            @JsonProperty(value = "maxMatches", required = true)
        int maxMatches,
            @JsonProperty(value = "cost", required = true)
        int cost,
            @JsonProperty(value = "preview", required = true)
        boolean preview,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject DraftFormatLibrary draftLibrary,
            @JacksonInject LeagueService leagueService) {
        _preview = preview;
        _leagueService = leagueService;
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyyMMdd"));
        ZonedDateTime startTime = startDate.atStartOfDay(ZoneId.of("UTC"));
        _league = new SoloDraftLeague(cost, name, 0, CollectionType.ALL_CARDS, cardBlueprintLibrary,
                startTime, seriesDuration, maxMatches, draftLibrary.getSoloDraft(draftType), System.currentTimeMillis(),
                formatLibrary);
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateLeagueAdmin(request);
        if (_preview) {
            Document doc = serializeLeague(_league);
            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } else {
            _leagueService.addLeague(_league);
            _leagueService.clearCache();
            responseWriter.writeJsonOkResponse();
        }
    }

}