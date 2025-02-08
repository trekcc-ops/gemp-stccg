package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.CacheManager;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.database.LeagueDAO;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.*;
import com.gempukku.stccg.service.AdminService;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
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
import java.util.StringJoiner;

public class AdminRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final static long CARD_LOAD_SLEEP_TIME = 6000;
    private final LeagueService _leagueService;
    private final CacheManager _cacheManager;
    private final HallServer _hallServer;
    private final FormatLibrary _formatLibrary;
    private final LeagueDAO _leagueDao;
    private final AdminService _adminService;
    private static final Logger LOGGER = LogManager.getLogger(AdminRequestHandler.class);

    AdminRequestHandler(ServerObjects objects) {
        super(objects);
        _leagueService = objects.getLeagueService();
        _cacheManager = objects.getCacheManager();
        _hallServer = objects.getHallServer();
        _formatLibrary = objects.getFormatLibrary();
        _leagueDao = objects.getLeagueDAO();
        _adminService = objects.getAdminService();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        String requestType = uri + request.method();
        switch(requestType) {
            case "/clearCachePOST":
                validateAdmin(request);
                clearCache(responseWriter);
                break;
            case "/shutdownPOST":
                validateAdmin(request);
                shutdown(request, responseWriter);
                break;
            case "/reloadCardsPOST":
                validateAdmin(request);
                reloadCards(responseWriter);
                break;
            case "/getDailyMessageGET":
                validateAdmin(request);
                getDailyMessage(request, responseWriter);
                break;
            case "/setDailyMessagePOST":
                validateAdmin(request);
                setDailyMessage(request, responseWriter);
                break;
            case "/previewSealedLeaguePOST":
                validateLeagueAdmin(request);
                previewSealedLeague(request, responseWriter);
                break;
            case "/addSealedLeaguePOST":
                validateLeagueAdmin(request);
                addSealedLeague(request, responseWriter);
                break;
            case "/previewConstructedLeaguePOST":
                validateLeagueAdmin(request);
                previewConstructedLeague(request, responseWriter);
                break;
            case "/addConstructedLeaguePOST":
                validateLeagueAdmin(request);
                addConstructedLeague(request, responseWriter);
                break;
            case "/previewSoloDraftLeaguePOST":
                validateLeagueAdmin(request);
                previewSoloDraftLeague(request, responseWriter);
                break;
            case "/addSoloDraftLeaguePOST":
                validateLeagueAdmin(request);
                addSoloDraftLeague(request, responseWriter);
                break;
            case "/banUserPOST":
                validateAdmin(request);
                banUser(request, responseWriter);
                break;
            case "/resetUserPasswordPOST":
                validateAdmin(request);
                resetUserPassword(request, responseWriter);
                break;
            case "/banMultiplePOST":
                validateAdmin(request);
                banMultiple(request, responseWriter);
                break;
            case "/banUserTempPOST":
                validateAdmin(request);
                banUserTemp(request, responseWriter);
                break;
            case "/unBanUserPOST":
                validateAdmin(request);
                unBanUser(request, responseWriter);
                break;
            case "/findMultipleAccountsPOST":
                validateAdmin(request);
                findMultipleAccounts(request, responseWriter);
                break;
            default:
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login).trim();

            PlayerDAO playerDAO = _serverObjects.getPlayerDAO();
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

    private void resetUserPassword(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            if (login == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            if (!_adminService.resetUserPassword(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void banUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            if (login==null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            if (!_adminService.banUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            List<String> logins = getLoginParametersSafely(postDecoder);
            if (logins.isEmpty())
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            for (String login : logins) {
                if (!_adminService.banUser(login))
                    throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            }
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void banUserTemp(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            int duration = Integer.parseInt(getFormParameterSafely(postDecoder, FormParameter.duration));
            if (!_adminService.banUserTemp(login, duration))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void unBanUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            if (!_adminService.unBanUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void addConstructedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String, String> parameters = getConstructedLeagueParameters(request);
        int cost = Integer.parseInt(parameters.get("cost"));

        LeagueData leagueData =
                new NewConstructedLeagueData(_cardBlueprintLibrary, _formatLibrary, parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);

        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                parameters.get("serializedParams"), leagueStart, displayEnd);
        _leagueService.clearCache();
        responseWriter.writeHtmlOkResponse();
    }

    private void previewConstructedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String,String> parameters = getConstructedLeagueParameters(request);
        LeagueData leagueData =
                new NewConstructedLeagueData(_cardBlueprintLibrary, _formatLibrary, parameters.get("serializedParams"));
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

    private void addSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException, NumberFormatException {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(_cardBlueprintLibrary,  _formatLibrary,
                _serverObjects.getSoloDraftDefinitions(), parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
        int cost = Integer.parseInt(parameters.get("cost"));

        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"),
                leagueData.getClass().getName(), parameters.get("serializedParams"), leagueStart, displayEnd);
        _leagueService.clearCache();

        responseWriter.writeHtmlOkResponse();
    }

    private void previewSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException, ParserConfigurationException {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(_cardBlueprintLibrary, _formatLibrary,
                _serverObjects.getSoloDraftDefinitions(), parameters.get("serializedParams"));
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

    private String serializeSealedLeagueParameters(Map<String,String> parameters) {
        return String.join(",", _formatLibrary.GetSealedTemplate(parameters.get("format")).getId(),
                parameters.get("start"), parameters.get("seriesDuration"), parameters.get("maxMatches"),
                parameters.get("code"), parameters.get("name"));
    }

    private void addSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        int cost = Integer.parseInt(parameters.get("cost"));
        String serializedParameters = serializeSealedLeagueParameters(parameters);
        LeagueData leagueData =
                new NewSealedLeagueData(_cardBlueprintLibrary, _formatLibrary, serializedParameters);
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                serializedParameters, leagueStart, displayEnd);
        _leagueService.clearCache();
        responseWriter.writeHtmlOkResponse();
    }

    private void getDailyMessage(HttpRequest request, ResponseWriter responseWriter) {
        try(SelfClosingPostRequestDecoder ignored = new SelfClosingPostRequestDecoder(request)) {
            String dailyMessage = _hallServer.getDailyMessage();
            if(dailyMessage != null)
                responseWriter.writeJsonResponse(HTMLUtils.replaceNewlines(dailyMessage));
        }
    }

    private void setDailyMessage(HttpRequest request, ResponseWriter responseWriter) throws IOException {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            _hallServer.setDailyMessage(getFormParameterSafely(postDecoder, FormParameter.messageOfTheDay));
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void shutdown(HttpRequest request, ResponseWriter responseWriter) {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            boolean shutdown = Boolean.parseBoolean(getFormParameterSafely(postDecoder, FormParameter.shutdown));
            _hallServer.setShutdown(shutdown);
            responseWriter.writeHtmlOkResponse();
        } catch (Exception e) {
            LOGGER.error("Error response for {}", request.uri(), e);
            responseWriter.writeHtmlResponse("Error handling request");
        }
    }

    private void reloadCards(ResponseWriter responseWriter) throws InterruptedException {
        ChatServer chatServer = _serverObjects.getChatServer();
        chatServer.sendSystemMessageToAllUsers(
                "Server is reloading card definitions.  This will impact game speed until it is complete.");
        Thread.sleep(CARD_LOAD_SLEEP_TIME);
        _cardBlueprintLibrary.reloadAllDefinitions();
        _serverObjects.getProductLibrary().ReloadPacks();
        _formatLibrary.reloadFormats(_cardBlueprintLibrary);
        _formatLibrary.reloadSealedTemplates();
        chatServer.sendSystemMessageToAllUsers(
                "Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page " +
                        "and please let the mod team know in the game hall ASAP if the problem doesn't go away.");
        responseWriter.writeHtmlOkResponse();
    }

    private void clearCache(ResponseWriter responseWriter) {
        _leagueService.clearCache();
        _serverObjects.getTournamentService().clearCache();
        int before = _cacheManager.getTotalCount();
        _cacheManager.clearCaches();
        int after = _cacheManager.getTotalCount();
        responseWriter.writeHtmlResponse("Before: " + before + "<br><br>After: " + after);
    }

    private void validateAdmin(HttpMessage request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request);
        if (!player.hasType(User.Type.ADMIN))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

    private void validateLeagueAdmin(HttpMessage request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request);
        if (!player.hasType(User.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

    private void previewSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        String serializedParameters = parameters.get("serializedParameters");
        CardBlueprintLibrary cardBlueprintLibrary = _serverObjects.getCardBlueprintLibrary();
        FormatLibrary formatLibrary = _serverObjects.getFormatLibrary();
        LeagueData leagueData = new NewSealedLeagueData(cardBlueprintLibrary, formatLibrary, serializedParameters);
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }
}