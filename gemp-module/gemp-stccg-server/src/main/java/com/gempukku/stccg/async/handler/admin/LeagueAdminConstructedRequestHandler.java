package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.ConstructedLeague;
import com.gempukku.stccg.league.LeagueService;
import org.w3c.dom.Document;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class LeagueAdminConstructedRequestHandler implements AdminRequestHandler, NewLeagueHandler {

    private final boolean _preview;
    private final LeagueService _leagueService;
    private final ConstructedLeague _league;

    LeagueAdminConstructedRequestHandler(
            @JsonProperty(value = "start", required = true)
        String start,
            @JsonProperty(value = "name", required = true)
        String name,
            @JsonProperty(value = "collectionType")
            CollectionType collectionType,
            @JsonProperty(value = "format")
        List<String> formats,
            @JsonProperty(value = "seriesDuration")
        List<Integer> seriesDurations,
            @JsonProperty("maxMatches")
        List<Integer> maxMatches,
            @JsonProperty("cost")
        String costString,
            @JsonProperty("preview")
        boolean preview,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject FormatLibrary formatLibrary,
            @JacksonInject LeagueService leagueService
            ) throws HttpProcessingException {
        _preview = preview;
        _leagueService = leagueService;
        if(start == null || start.trim().isEmpty()
                || collectionType == null
                || name == null || name.trim().isEmpty()
                || costString == null || costString.trim().isEmpty()) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
        LocalDateTime startTimeLocal = LocalDateTime.parse(start);
        ZonedDateTime startTimeUTC = startTimeLocal.atZone(ZoneId.of("UTC"));

        if(formats.size() != seriesDurations.size() || formats.size() != maxMatches.size())
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        _league = new ConstructedLeague(Integer.parseInt(costString), name, collectionType,
                formatLibrary, cardBlueprintLibrary, seriesDurations, maxMatches, formats, startTimeUTC);
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