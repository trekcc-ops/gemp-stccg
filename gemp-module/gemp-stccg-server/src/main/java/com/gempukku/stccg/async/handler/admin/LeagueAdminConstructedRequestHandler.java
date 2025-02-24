package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewConstructedLeagueData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.StringJoiner;

public class LeagueAdminConstructedRequestHandler extends AdminRequestHandlerNew implements UriRequestHandler {

    // TODO - This doesn't work

    private final long _creationTime;
    private final String _start;
    private final List<String> _maxMatches;
    private final String _name;
    private final List<String> _seriesDurations;
    private final String _collectionType;
    private final String _prizeMultiplier;
    private final String _costString;
    private final List<String> _formats;
    private final boolean _preview;

    LeagueAdminConstructedRequestHandler(
        @JsonProperty(value = "start", required = true)
        String start,
        @JsonProperty(value = "name", required = true)
        String name,
        @JsonProperty(value = "collectionType")
        String collectionType,
        @JsonProperty(value = "prizeMultiplier")
        String prizeMultiplier,
        @JsonProperty(value = "format")
        List<String> formats,
        @JsonProperty(value = "seriesDuration")
        List<String> seriesDurations,
        @JsonProperty("maxMatches")
        List<String> maxMatches,
        @JsonProperty("cost")
        String costString,
        @JsonProperty("preview")
        boolean preview
    ) {
        _creationTime = System.currentTimeMillis();
        _start = start;
        _seriesDurations = seriesDurations;
        _maxMatches = maxMatches;
        _name = name;
        _collectionType = collectionType;
        _prizeMultiplier = prizeMultiplier;
        _costString = costString;
        _formats = formats;
        _preview = preview;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateLeagueAdmin(request);

        if(_start == null || _start.trim().isEmpty()
                || _collectionType == null || _collectionType.trim().isEmpty()
                || _prizeMultiplier == null || _prizeMultiplier.trim().isEmpty()
                || _name == null || _name.trim().isEmpty()
                || _costString == null || _costString.trim().isEmpty()) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }

        if(_formats.size() != _seriesDurations.size() || _formats.size() != _maxMatches.size())
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

        StringJoiner sj = new StringJoiner(",");
        sj.add(_start).add(_collectionType).add(_prizeMultiplier).add("1").add(Integer.toString(_formats.size()));
        for (int i = 0; i < _formats.size(); i++)
            sj.add(_formats.get(i)).add(_seriesDurations.get(i)).add(_maxMatches.get(i));
        String serializedParams = sj.toString();

        CardBlueprintLibrary cardBlueprintLibrary = serverObjects.getCardBlueprintLibrary();
        FormatLibrary formatLibrary = serverObjects.getFormatLibrary();
        LeagueData leagueData = new NewConstructedLeagueData(cardBlueprintLibrary, formatLibrary, serializedParams);

        if (_preview) {
            Document doc = serializeLeague(leagueData);
            responseWriter.writeXmlResponseWithNoHeaders(doc);
        } else {
            int cost = Integer.parseInt(_costString);
            List<LeagueSeriesData> series = leagueData.getSeries();
            int leagueStart = series.getFirst().getStart();
            int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);

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