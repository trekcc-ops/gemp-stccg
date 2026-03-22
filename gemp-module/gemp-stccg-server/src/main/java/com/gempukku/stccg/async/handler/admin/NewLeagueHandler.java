package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeries;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NewLeagueHandler extends UriRequestHandler {

    default Document serializeLeague(League league)
            throws ParserConfigurationException {
        Document doc = createNewDoc();

        Element leagueElem = doc.createElement("league");
        final List<LeagueSeries> allSeries = league.getAllSeries();

        leagueElem.setAttribute("name", league.getName());
        leagueElem.setAttribute("cost", String.valueOf(league.getCost()));
        leagueElem.setAttribute("start", String.valueOf(league.getStart()));
        leagueElem.setAttribute("end", String.valueOf(league.getEnd()));

        for (LeagueSeries series : allSeries) {
            Element seriesElem = doc.createElement("series");
            seriesElem.setAttribute("type", series.getName());
            seriesElem.setAttribute("maxMatches", String.valueOf(series.getMaxMatches()));
            seriesElem.setAttribute("start", String.valueOf(series.getStart()));
            seriesElem.setAttribute("end", String.valueOf(series.getEnd()));
            seriesElem.setAttribute("format", series.getFormat().getName());
            seriesElem.setAttribute("collection", league.getCollectionType().getFullName());
            seriesElem.setAttribute("limited", String.valueOf(league.isLimited()));

            leagueElem.appendChild(seriesElem);
        }
        doc.appendChild(leagueElem);
        return doc;
    }

    default String getSerializedLeagueData(League league) throws JsonProcessingException {
        List<LeagueSeries> allSeries = league.getAllSeries();
        Map<Object, Object> result = new HashMap<>();
        result.put("name", league.getName());
        result.put("start", league.getStart());
        result.put("end", league.getEnd());
        List<Map<Object, Object>> seriesParameters = new ArrayList<>();
        for (LeagueSeries series : allSeries) {
            Map<Object, Object> map = new HashMap<>();
            map.put("type", series.getName());
            map.put("maxMatches", series.getMaxMatches());
            map.put("start", series.getStart());
            map.put("end", series.getEnd());
            map.put("format", series.getFormat().getName());
            map.put("collection", league.getCollectionType().getFullName());
            map.put("limited", league.isLimited());
            seriesParameters.add(map);
        }
        result.put("series", seriesParameters);
        return new ObjectMapper().writeValueAsString(result);
    }


}