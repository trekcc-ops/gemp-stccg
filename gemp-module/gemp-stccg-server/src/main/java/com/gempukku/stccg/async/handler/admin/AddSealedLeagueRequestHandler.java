package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.league.SealedLeague;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AddSealedLeagueRequestHandler implements UriRequestHandler, AdminRequestHandler {
    private final LeagueService _leagueService;
    private final SealedLeague _league;

    AddSealedLeagueRequestHandler(
            @JsonProperty(value = "format", required = true)
        String format,
            @JsonProperty(value = "start", required = true)
        int start,
            @JsonProperty(value = "seriesDuration", required = true)
        int seriesDuration,
            @JsonProperty(value = "maxMatches", required = true)
        int maxMatches,
            @JsonProperty(value = "name", required = true)
        String name,
            @JsonProperty(value = "cost", required = true)
            int cost,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject LeagueService leagueService) {
        _leagueService = leagueService;
        LocalDate startDate = LocalDate.parse(String.valueOf(start), DateTimeFormatter.ofPattern("yyyyMMdd"));
        ZonedDateTime startTime = startDate.atStartOfDay(ZoneId.of("UTC"));
        _league = new SealedLeague(format, CollectionType.ALL_CARDS, cardBlueprintLibrary, startTime, formatLibrary,
                cost, name, 0, seriesDuration, maxMatches, -999, 4);
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateLeagueAdmin(request);
        _leagueService.addLeague(_league);
        _leagueService.clearCache();
        responseWriter.writeJsonOkResponse();
    }

}