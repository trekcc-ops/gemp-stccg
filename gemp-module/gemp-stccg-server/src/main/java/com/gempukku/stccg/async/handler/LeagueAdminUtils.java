package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewSealedLeagueData;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class LeagueAdminUtils {
    private static final Logger LOGGER = LogManager.getLogger(LeagueAdminUtils.class);

    static void previewSealedLeague(HttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects objects) throws Exception {
        Map<String,String> parameters =
                getSoloDraftOrSealedLeagueParameters(request, objects.getPlayerDAO(), objects.getLoggedUserHolder());
        String serializedParameters = parameters.get("serializedParameters");
        CardBlueprintLibrary cardBlueprintLibrary = objects.getCardBlueprintLibrary();
        FormatLibrary formatLibrary = objects.getFormatLibrary();
        LeagueData leagueData = new NewSealedLeagueData(cardBlueprintLibrary, formatLibrary, serializedParameters);
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private static Map<String,String> getSoloDraftOrSealedLeagueParameters(HttpRequest request, PlayerDAO playerDAO,
                                                                           LoggedUserHolder loggedUserHolder)
            throws HttpProcessingException, IOException {
        validateLeagueAdmin(request, playerDAO, loggedUserHolder);
        String[] parameterNames = {"format", "start", "seriesDuration", "maxMatches", "name", "cost"};
        Map<String, String> parameterMap = new HashMap<>();
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);

        try {
            for (String parameterName : parameterNames) {
                String value = DOMUtils.getFormParameterSafely(postDecoder, parameterName);
                if (value == null || value.trim().isEmpty())
                    throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
                else
                    parameterMap.put(parameterName, value);
            }
            parameterMap.put("code", String.valueOf(System.currentTimeMillis()));
            String serializedParams = String.join(",", parameterMap.get("format"), parameterMap.get("start"),
                    parameterMap.get("seriesDuration"), parameterMap.get("maxMatches"), parameterMap.get("code"),
                    parameterMap.get("name"));
            parameterMap.put("serializedParams", serializedParams);
            return parameterMap;
        } catch (RuntimeException ex) {
            HttpUtils.logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, request.uri(), ex);
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        } finally {
            postDecoder.destroy();
        }
    }

    private static void writeLeagueDocument(ResponseWriter responseWriter, LeagueData leagueData,
                                            Map<String, String> leagueParameters)
            throws ParserConfigurationException {
        Document doc = DOMUtils.createNewDoc();

        int cost = Integer.parseInt(leagueParameters.get("cost"));
        Element leagueElem = doc.createElement("league");
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();
        int end = allSeries.getLast().getEnd();

        leagueElem.setAttribute("name", leagueParameters.get("name"));
        leagueElem.setAttribute("cost", String.valueOf(cost));
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
        responseWriter.writeXmlResponse(doc);
    }

    private static void validateLeagueAdmin(HttpMessage request, PlayerDAO playerDAO,
                                            LoggedUserHolder loggedUserHolder) throws HttpProcessingException {
        User player = DOMUtils.getResourceOwnerSafely(request, null, playerDAO, loggedUserHolder);

        if (!player.hasType(User.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }


}