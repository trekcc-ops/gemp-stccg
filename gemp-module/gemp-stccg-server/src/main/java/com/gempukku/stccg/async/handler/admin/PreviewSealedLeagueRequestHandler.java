package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.SealedLeague;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PreviewSealedLeagueRequestHandler implements AdminRequestHandler, NewLeagueHandler {
    private final String _leagueTemplateName;
    private final String _name;
    private final int _cost;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final FormatLibrary _formatLibrary;
    private final ZonedDateTime _startTime;
    private final int _seriesDuration;
    private final int _seriesCount;
    private final int _maxMatches;
    private final Clock _systemClock = Clock.systemUTC();

    PreviewSealedLeagueRequestHandler(
        @JsonProperty(value = "format", required = true)
        String format,
        @JsonProperty(value = "cost", required = true)
        int cost,
        @JsonProperty(value = "start", required = true)
        String start,
        @JsonProperty(value = "name", required = true)
        String name,
        @JsonProperty(value = "duration", required = true)
        int duration,
        @JsonProperty(value = "maxMatches", required = true)
        int maxMatches,
        @JsonProperty(value = "seriesCount", required = true)
        int seriesCount,
        @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
        @JacksonInject FormatLibrary formatLibrary
    ) {
        _leagueTemplateName = format;
        _name = name;
        _cost = cost;
        _seriesDuration = duration;
        _maxMatches = maxMatches;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _formatLibrary = formatLibrary;
        _seriesCount = seriesCount;
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyyMMdd"));
        _startTime = startDate.atStartOfDay(_systemClock.getZone());
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateLeagueAdmin(request);
        League league = new SealedLeague(_leagueTemplateName, CollectionType.ALL_CARDS, _cardBlueprintLibrary,
                _startTime, _formatLibrary, _cost, _name, 0, _seriesDuration, _maxMatches, -999,
                _seriesCount, _systemClock);
        responseWriter.writeJsonResponse(getSerializedLeagueData(league));
    }

}