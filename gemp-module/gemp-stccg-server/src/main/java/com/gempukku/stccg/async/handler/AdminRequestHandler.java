package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.admin.AdminRequestHandlerNew;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.LeagueData;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.NewConstructedLeagueData;
import com.gempukku.stccg.league.SoloDraftLeagueData;
import com.gempukku.stccg.service.AdminService;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

public class AdminRequestHandler extends AdminRequestHandlerNew {
    private static final Logger LOGGER = LogManager.getLogger(AdminRequestHandler.class);

    public final void handleRequest(String uri, GempHttpRequest gempRequest,
                                    ResponseWriter responseWriter, ServerObjects serverObjects) throws Exception {
        HttpRequest request = gempRequest.getRequest();
        String requestType = uri + request.method();
        switch(requestType) {
            case "/getDailyMessageGET":
                validateAdmin(gempRequest);
                getDailyMessage(request, responseWriter, serverObjects.getHallServer());
                break;
            case "/setDailyMessagePOST":
                validateAdmin(gempRequest);
                setDailyMessage(request, responseWriter, serverObjects.getHallServer());
                break;
            case "/previewConstructedLeaguePOST":
                validateLeagueAdmin(gempRequest);
                previewConstructedLeague(request, responseWriter, serverObjects);
                break;
            case "/addConstructedLeaguePOST":
                validateLeagueAdmin(gempRequest);
                addConstructedLeague(request, responseWriter, serverObjects);
                break;
            case "/previewSoloDraftLeaguePOST":
                validateLeagueAdmin(gempRequest);
                previewSoloDraftLeague(request, responseWriter, serverObjects);
                break;
            case "/addSoloDraftLeaguePOST":
                validateLeagueAdmin(gempRequest);
                addSoloDraftLeague(request, responseWriter, serverObjects);
                break;
            case "/resetUserPasswordPOST":
                validateAdmin(gempRequest);
                resetUserPassword(request, responseWriter, serverObjects.getAdminService());
                break;
            case "/banMultiplePOST":
                validateAdmin(gempRequest);
                banMultiple(request, responseWriter, serverObjects.getAdminService());
                break;
            case "/findMultipleAccountsPOST":
                validateAdmin(gempRequest);
                findMultipleAccounts(request, responseWriter, serverObjects.getPlayerDAO());
                break;
            default:
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter, PlayerDAO playerDAO) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login).trim();

            List<User> similarPlayers = playerDAO.findSimilarAccounts(login);
            if (similarPlayers == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            Document doc = createNewDoc();
            Element players = doc.createElement("players");
            for (User similarPlayer : similarPlayers) {
                Element playerElem = doc.createElement("player");
                playerElem.setAttribute("id", String.valueOf(similarPlayer.getId()));
                playerElem.setAttribute("name", similarPlayer.getName());
                playerElem.setAttribute("password", similarPlayer.getPassword());
                playerElem.setAttribute("status", similarPlayer.getStatus());
                playerElem.setAttribute("createIp", similarPlayer.getCreateIp());
                playerElem.setAttribute("loginIp", similarPlayer.getLastIp());
                players.appendChild(playerElem);
            }
            doc.appendChild(players);
            responseWriter.writeXmlResponseWithNoHeaders(doc);
        }
    }

    private void resetUserPassword(HttpRequest request, ResponseWriter responseWriter, AdminService adminService) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            if (login == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            if (!adminService.resetUserPassword(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            responseWriter.writeJsonOkResponse();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter, AdminService adminService) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            List<String> logins = getLoginParametersSafely(postDecoder);
            if (logins.isEmpty())
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            for (String login : logins) {
                if (!adminService.banUser(login))
                    throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            }
            responseWriter.writeJsonOkResponse();
        }
    }

    static List<String> getLoginParametersSafely(InterfaceHttpPostRequestDecoder postRequestDecoder)
            throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        return getFormMultipleParametersSafely(postRequestDecoder,"login[]");
    }

    static List<String> getFormMultipleParametersSafely(InterfaceHttpPostRequestDecoder postRequestDecoder,
                                                        String parameterName)
            throws HttpPostRequestDecoder.NotEnoughDataDecoderException, IOException {
        List<String> result = new LinkedList<>();
        List<InterfaceHttpData> dataList = postRequestDecoder.getBodyHttpDatas(parameterName);
        if (dataList == null)
            return Collections.emptyList();
        for (InterfaceHttpData data : dataList) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                result.add(attribute.getValue());
            }

        }
        return result;
    }



    private void addConstructedLeague(HttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects) throws Exception {
        Map<String, String> parameters = getConstructedLeagueParameters(request);
        int cost = Integer.parseInt(parameters.get("cost"));

        LeagueData leagueData =
                new NewConstructedLeagueData(serverObjects.getCardBlueprintLibrary(), serverObjects.getFormatLibrary(), parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);

        serverObjects.getLeagueDAO().addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                parameters.get("serializedParams"), leagueStart, displayEnd);
        serverObjects.getLeagueService().clearCache();
        responseWriter.writeJsonOkResponse();
    }

    private void previewConstructedLeague(HttpRequest request, ResponseWriter responseWriter,
                                          ServerObjects serverObjects) throws Exception {
        Map<String,String> parameters = getConstructedLeagueParameters(request);
        LeagueData leagueData =
                new NewConstructedLeagueData(serverObjects.getCardBlueprintLibrary(), serverObjects.getFormatLibrary(),
                        parameters.get("serializedParams"));
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private static void writeLeagueDocument(ResponseWriter responseWriter, LeagueData leagueData,
                                            Map<String, String> leagueParameters)
            throws ParserConfigurationException {
        Document doc = createNewDoc();

        int cost = Integer.parseInt(leagueParameters.get(FormParameter.cost.name()));
        Element leagueElem = doc.createElement("league");
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();
        int end = allSeries.getLast().getEnd();

        leagueElem.setAttribute(FormParameter.name.name(), leagueParameters.get(FormParameter.name.name()));
        leagueElem.setAttribute(FormParameter.cost.name(), String.valueOf(cost));
        leagueElem.setAttribute(FormParameter.start.name(), String.valueOf(allSeries.getFirst().getStart()));
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
        responseWriter.writeXmlResponseWithNoHeaders(doc);
    }

    private void addSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws HttpProcessingException, IOException, NumberFormatException {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(serverObjects.getCardBlueprintLibrary(),  serverObjects.getFormatLibrary(),
                serverObjects.getSoloDraftDefinitions(), parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
        int cost = Integer.parseInt(parameters.get("cost"));

        serverObjects.getLeagueDAO().addLeague(cost, parameters.get("name"), parameters.get("code"),
                leagueData.getClass().getName(), parameters.get("serializedParams"), leagueStart, displayEnd);
        serverObjects.getLeagueService().clearCache();

        responseWriter.writeJsonOkResponse();
    }

    private void previewSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws HttpProcessingException, IOException, ParserConfigurationException {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(serverObjects.getCardBlueprintLibrary(),
                serverObjects.getFormatLibrary(), serverObjects.getSoloDraftDefinitions(),
                parameters.get("serializedParams"));
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private Map<String,String> getSoloDraftOrSealedLeagueParameters(HttpRequest request)
            throws HttpProcessingException, IOException {
        FormParameter[] parameterNames = {
                FormParameter.format, FormParameter.start, FormParameter.seriesDuration,
                FormParameter.maxMatches, FormParameter.name, FormParameter.cost
        };
        Map<String, String> parameterMap = new HashMap<>();
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            for (FormParameter parameterName : parameterNames) {
                String value = getFormParameterSafely(postDecoder, parameterName);
                if (value == null || value.trim().isEmpty())
                    throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
                else
                    parameterMap.put(parameterName.name(), value);
            }
            parameterMap.put("code", String.valueOf(System.currentTimeMillis()));
            String serializedParams = String.join(",", parameterMap.get("format"), parameterMap.get("start"),
                    parameterMap.get("seriesDuration"), parameterMap.get("maxMatches"), parameterMap.get("code"),
                    parameterMap.get("name"));
            parameterMap.put("serializedParams", serializedParams);
            return parameterMap;
        } catch (RuntimeException ex) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, request.uri(), ex);
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }

    private Map<String,String> getConstructedLeagueParameters(HttpRequest request)
            throws HttpProcessingException, IOException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String start = getFormParameterSafely(postDecoder, FormParameter.start);
            String collectionType = getFormParameterSafely(postDecoder, FormParameter.collectionType);
            String prizeMultiplier = getFormParameterSafely(postDecoder, FormParameter.prizeMultiplier);
            List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
            List<String> seriesDurations = getFormMultipleParametersSafely(postDecoder, "seriesDuration[]");
            List<String> maxMatches = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");
            String name = getFormParameterSafely(postDecoder, FormParameter.name);
            String costStr = getFormParameterSafely(postDecoder, FormParameter.cost);

            if(start == null || start.trim().isEmpty()
                    ||collectionType == null || collectionType.trim().isEmpty()
                    ||prizeMultiplier == null || prizeMultiplier.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            }

            if(formats.size() != seriesDurations.size() || formats.size() != maxMatches.size())
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            //The 1 is a hard-coded maximum number of player matches per league.
            // Original LotR comments had a note that this should be added to the UI "properly".
            // Since that time, the LotR code has dropped reliance on this method.
            StringJoiner sj = new StringJoiner(",");
            sj.add(start).add(collectionType).add(prizeMultiplier).add("1").add(Integer.toString(formats.size()));
            for (int i = 0; i < formats.size(); i++)
                sj.add(formats.get(i)).add(seriesDurations.get(i)).add(maxMatches.get(i));

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("name", name);
            parameterMap.put("cost", costStr);
            parameterMap.put("serializedParams", sj.toString());
            parameterMap.put("code", String.valueOf(System.currentTimeMillis()));
            return parameterMap;

        } catch (RuntimeException ex) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, request.uri(), ex);
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }

    private void getDailyMessage(HttpRequest request, ResponseWriter responseWriter, HallServer hallServer) {
        try(SelfClosingPostRequestDecoder ignored = new SelfClosingPostRequestDecoder(request)) {
            String dailyMessage = hallServer.getDailyMessage();
            if(dailyMessage != null)
                responseWriter.writeJsonResponse(HTMLUtils.replaceNewlines(dailyMessage));
        }
    }

    private void setDailyMessage(HttpRequest request, ResponseWriter responseWriter, HallServer hallServer) throws IOException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            hallServer.setDailyMessage(getFormParameterSafely(postDecoder, FormParameter.messageOfTheDay));
            responseWriter.writeJsonOkResponse();
        }
    }

    protected static class SelfClosingPostRequestDecoder extends HttpPostRequestDecoder implements AutoCloseable {

        SelfClosingPostRequestDecoder(HttpRequest request) {
            super(request);
        }

        @Override
        public void close() {
            destroy();
        }
    }

    protected enum FormParameter {
        availablePicks, blueprintId, cardId, channelNumber, choiceId, collectionType,
        cost, count, decisionId, decisionValue,
        deck, deckContents, decklist, deckName, decks,
        desc, duration, filter, format, id, includeEvents, isInviteOnly, isPrivate, length, login, maxMatches,
        message, messageOfTheDay, name, notes, oldDeckName,
        ownedMin, pack, participantId, password, players, price, prizeMultiplier, product, reason, selection,
        seriesDuration, shutdown, start, startDay, targetFormat, timer
    }

    static String getFormParameterSafely(InterfaceHttpPostRequestDecoder decoder, FormParameter parameter)
            throws IOException {
        InterfaceHttpData data = decoder.getBodyHttpData(parameter.name());
        if (data == null)
            return null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            return attribute.getValue();
        } else {
            return null;
        }
    }

    static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code % 400 < 100 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            log.debug("HTTP {} response for {}", code, uri);

            // record an HTTP 400
        else if(code == HttpURLConnection.HTTP_BAD_REQUEST || code % 500 < 100)
            log.error("HTTP code {} response for {}", code, uri, exp);
    }




}