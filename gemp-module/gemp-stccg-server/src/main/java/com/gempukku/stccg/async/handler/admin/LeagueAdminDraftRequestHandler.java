package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.SoloDraftLeagueData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LeagueAdminDraftRequestHandler extends AdminRequestHandlerNew implements UriRequestHandler {

    // TODO - This doesn't work

    private final long _creationTime;
    private final String _start;
    private final String _maxMatches;
    private final String _name;
    private final String _seriesDuration;
    private final String _costString;
    private final String _format;
    private final boolean _preview;

    LeagueAdminDraftRequestHandler(
        @JsonProperty(value = "start", required = true)
        String start,
        @JsonProperty(value = "name", required = true)
        String name,
        @JsonProperty(value = "format", required = true)
        String format,
        @JsonProperty(value = "seriesDuration", required = true)
        String seriesDuration,
        @JsonProperty(value = "maxMatches", required = true)
        String maxMatches,
        @JsonProperty(value = "cost", required = true)
        String costString,
        @JsonProperty(value = "preview", required = true)
        boolean preview
    ) {
        _creationTime = System.currentTimeMillis();
        _start = start;
        _seriesDuration = seriesDuration;
        _maxMatches = maxMatches;
        _name = name;
        _costString = costString;
        _format = format;
        _preview = preview;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateLeagueAdmin(request);
        String serializedParams = String.join(",", _format, _start, _seriesDuration, _maxMatches,
                String.valueOf(_creationTime), _name);

        LeagueData leagueData = new SoloDraftLeagueData(serverObjects.getCardBlueprintLibrary(),
                serverObjects.getFormatLibrary(), serverObjects.getSoloDraftDefinitions(),
                serializedParams);

        if (_preview) {
            Document doc = serializeLeague(leagueData);
            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } else {
            List<LeagueSeriesData> series = leagueData.getSeries();
            int leagueStart = series.getFirst().getStart();
            int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
            int cost = Integer.parseInt(_costString);

            serverObjects.getLeagueDAO().addLeague(cost, _name, String.valueOf(_creationTime),
                    leagueData.getClass().getName(), serializedParams, leagueStart, displayEnd);
            serverObjects.getLeagueService().clearCache();

            responseWriter.writeJsonOkResponse();
        }
    }

    private Document serializeLeague(LeagueData leagueData)
            throws ParserConfigurationException {
        Document doc = createNewDoc();

        Element leagueElem = doc.createElement("league");
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();
        int end = allSeries.getLast().getEnd();

        leagueElem.setAttribute("name", _name);
        leagueElem.setAttribute("cost", _costString);
        leagueElem.setAttribute("start", String.valueOf(allSeries.getFirst().getStart()));
        leagueElem.setAttribute("end", String.valueOf(end));

        for (LeagueSeriesData series : allSeries) {
            Element seriesElem = doc.createElement("series");
            seriesElem.setAttribute("type", series.getName());
            seriesElem.setAttribute("maxMatches", String.valueOf(series.getMaxMatches()));
            seriesElem.setAttribute("start", String.valueOf(series.getStart()));
            seriesElem.setAttribute("end", String.valueOf(series.getEnd()));
            seriesElem.setAttribute("format", series.getFormat().getName());
            seriesElem.setAttribute("collection", series.getCollectionType().getFullName());
            seriesElem.setAttribute("limited", String.valueOf(series.isLimited()));

            leagueElem.appendChild(seriesElem);
        }
        doc.appendChild(leagueElem);
        return doc;
    }

}