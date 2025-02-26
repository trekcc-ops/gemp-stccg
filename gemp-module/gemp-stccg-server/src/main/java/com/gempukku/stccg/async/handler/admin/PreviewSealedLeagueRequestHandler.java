package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewSealedLeagueData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties("cost")
public class PreviewSealedLeagueRequestHandler implements UriRequestHandler, AdminRequestHandler {
    private final long _creationTime;
    private final int _start;
    private final String _leagueTemplateName;
    private final int _maxMatches;
    private final String _name;
    private final int _seriesDuration;

    PreviewSealedLeagueRequestHandler(
        @JsonProperty(value = "format", required = true)
        String format,
        @JsonProperty(value = "start", required = true)
        int start,
        @JsonProperty(value = "seriesDuration", required = true)
        int seriesDuration,
        @JsonProperty(value = "maxMatches", required = true)
        int maxMatches,
        @JsonProperty(value = "name", required = true)
        String name
    ) {
        _creationTime = System.currentTimeMillis();
        _leagueTemplateName = format;
        _start = start;
        _seriesDuration = seriesDuration;
        _maxMatches = maxMatches;
        _name = name;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateLeagueAdmin(request);
        CardBlueprintLibrary cardBlueprintLibrary = serverObjects.getCardBlueprintLibrary();
        FormatLibrary formatLibrary = serverObjects.getFormatLibrary();
        LeagueData leagueData = new NewSealedLeagueData(cardBlueprintLibrary, formatLibrary, _leagueTemplateName,
                _start, _seriesDuration, _maxMatches, _creationTime, _name);
        responseWriter.writeJsonResponse(getSerializedLeagueData(leagueData));
    }

    private String getSerializedLeagueData(LeagueData leagueData) throws JsonProcessingException {
        List<LeagueSeriesData> allSeries = leagueData.getSeries();
        int end = allSeries.getLast().getEnd();
        Map<Object, Object> result = new HashMap<>();
        result.put("name", _leagueTemplateName);
        result.put("start", convertIntDateToString(allSeries.getFirst().getStart()));
        result.put("end", convertIntDateToString(end));
        List<Map<Object, Object>> seriesParameters = new ArrayList<>();
        for (LeagueSeriesData series : allSeries) {
            Map<Object, Object> map = new HashMap<>();
            map.put("type", series.getName());
            map.put("maxMatches", series.getMaxMatches());
            map.put("start", convertIntDateToString(series.getStart()));
            map.put("end", convertIntDateToString(series.getEnd()));
            map.put("format", series.getFormat().getName());
            map.put("collection", series.getCollectionType().getFullName());
            map.put("limited", series.isLimited());
            seriesParameters.add(map);
        }
        result.put("series", seriesParameters);
        return new ObjectMapper().writeValueAsString(result);
    }

    private String convertIntDateToString(int date) {
        String fullDateString = String.valueOf(date);
        return fullDateString.substring(0,4) + "-" + fullDateString.substring(4,6) + "-" + fullDateString.substring(6,8);
    }
}