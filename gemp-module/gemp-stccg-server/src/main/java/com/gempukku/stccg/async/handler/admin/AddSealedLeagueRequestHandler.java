package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewSealedLeagueData;

import java.util.List;

@JsonIgnoreProperties("cost")
public class AddSealedLeagueRequestHandler extends AdminRequestHandlerNew implements UriRequestHandlerNew {
    private final long _creationTime;
    private final int _start;
    private final String _leagueTemplateName;
    private final int _maxMatches;
    private final String _name;
    private final int _seriesDuration;

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
        NewSealedLeagueData leagueData = new NewSealedLeagueData(cardBlueprintLibrary, formatLibrary, _leagueTemplateName,
                _start, _seriesDuration, _maxMatches, _creationTime, _name);
        List<LeagueSeriesData> series = leagueData.getSeries();
        serverObjects.getLeagueDAO().addLeague(series, leagueData);
        serverObjects.getLeagueService().clearCache();
        responseWriter.writeJsonOkResponse();
    }

}