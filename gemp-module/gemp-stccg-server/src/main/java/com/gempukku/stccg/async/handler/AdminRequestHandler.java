package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.CacheManager;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.*;
import com.gempukku.stccg.service.AdminService;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final static long CARD_LOAD_SLEEP_TIME = 6000;
    private final LeagueService _leagueService;
    private final CacheManager _cacheManager;
    private final HallServer _hallServer;
    private final FormatLibrary _formatLibrary;
    private final LeagueDAO _leagueDao;
    private final CollectionsManager _collectionManager;
    private final AdminService _adminService;
    private static final Logger LOGGER = LogManager.getLogger(AdminRequestHandler.class);

    AdminRequestHandler(ServerObjects objects) {
        super(objects);
        _leagueService = objects.getLeagueService();
        _cacheManager = objects.getCacheManager();
        _hallServer = objects.getHallServer();
        _formatLibrary = objects.getFormatLibrary();
        _leagueDao = objects.getLeagueDAO();
        _collectionManager = objects.getCollectionsManager();
        _adminService = objects.getAdminService();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        String requestType = uri + request.method();
        switch(requestType) {
            case "/clearCachePOST":
                clearCache(request, responseWriter);
                break;
            case "/shutdownPOST":
                shutdown(request, responseWriter);
                break;
            case "/reloadCardsPOST":
                reloadCards(request, responseWriter);
                break;
            case "/getDailyMessageGET":
                getDailyMessage(request, responseWriter);
                break;
            case "/setDailyMessagePOST":
                setDailyMessage(request, responseWriter);
                break;
            case "/previewSealedLeaguePOST":
                LeagueAdminUtils.previewSealedLeague(request, responseWriter, _serverObjects);
                break;
            case "/addSealedLeaguePOST":
                addSealedLeague(request, responseWriter);
                break;
            case "/previewConstructedLeaguePOST":
                previewConstructedLeague(request, responseWriter);
                break;
            case "/addConstructedLeaguePOST":
                addConstructedLeague(request, responseWriter);
                break;
            case "/previewSoloDraftLeaguePOST":
                previewSoloDraftLeague(request, responseWriter);
                break;
            case "/addSoloDraftLeaguePOST":
                addSoloDraftLeague(request, responseWriter);
                break;
            case "/addItemsPOST":
                addItems(request, responseWriter);
                break;
            case "/addItemsToCollectionPOST":
                addItemsToCollection(request, responseWriter);
                break;
            case "/banUserPOST":
                banUser(request, responseWriter);
                break;
            case "/resetUserPasswordPOST":
                resetUserPassword(request, responseWriter);
                break;
            case "/banMultiplePOST":
                banMultiple(request, responseWriter);
                break;
            case "/banUserTempPOST":
                banUserTemp(request, responseWriter);
                break;
            case "/unBanUserPOST":
                unBanUser(request, responseWriter);
                break;
            case "/findMultipleAccountsPOST":
                findMultipleAccounts(request, responseWriter);
                break;
            default:
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login").trim();

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
                playerElem.setAttribute("status", getStatus(similarPlayer));
                playerElem.setAttribute("createIp", similarPlayer.getCreateIp());
                playerElem.setAttribute("loginIp", similarPlayer.getLastIp());
                players.appendChild(playerElem);
            }

            doc.appendChild(players);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private static String getStatus(User similarPlayer) {
        if (similarPlayer.getType().isEmpty())
            return "Banned permanently";
        if (similarPlayer.getBannedUntil() != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return "Banned until " + format.format(similarPlayer.getBannedUntil());
        }
        if (similarPlayer.hasType(User.Type.UNBANNED))
            return "Unbanned";
        return "OK";
    }

    private void resetUserPassword(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            if (!_adminService.resetUserPassword(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            if (!_adminService.banUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            List<String> logins = getLoginParametersSafely(postDecoder);
            if (logins == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400

            for (String login : logins) {
                if (!_adminService.banUser(login))
                    throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            }

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUserTemp(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");
            int duration = Integer.parseInt(getFormParameterSafely(postDecoder, "duration"));

            if (!_adminService.banUserTemp(login, duration))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void unBanUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (!_adminService.unBanUser(login))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItemsToCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String reason = getFormParameterSafely(postDecoder, "reason");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<GenericCardItem> productItems = getProductItems(product);

            Map<User, CardCollection> playersCollection = _collectionManager.getPlayersCollection(collectionType);

            for (Map.Entry<User, CardCollection> playerCollection : playersCollection.entrySet()) {
                User key = playerCollection.getKey();
                CollectionType collectionType1 = createCollectionType(collectionType);
                _collectionManager.addItemsToPlayerCollection(
                        true, reason, key, collectionType1, productItems);
            }

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItems(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String players = getFormParameterSafely(postDecoder, "players");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<GenericCardItem> productItems = getProductItems(product);

            List<String> playerNames = getItems(players);

            for (String playerName : playerNames) {
                User player = _playerDao.getPlayer(playerName);

            _collectionManager.addItemsToPlayerCollection(true,
                    "Administrator action", player, createCollectionType(collectionType), productItems);
        }

        responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private static List<String> getItems(String values) {
        List<String> result = new LinkedList<>();
        for (String pack : values.split("\n")) {
            String blueprint = pack.trim();
            if (!blueprint.isEmpty())
                result.add(blueprint);
        }
        return result;
    }

    private static Collection<GenericCardItem> getProductItems(String values) {
        Collection<GenericCardItem> result = new LinkedList<>();
        for (String item : values.split("\n")) {
            String strippedItem = item.strip();
            if (!strippedItem.isEmpty()) {
                final String[] itemSplit = strippedItem.split("x", 2);
                if (itemSplit.length != 2)
                    throw new RuntimeException("Unable to parse the items");
                result.add(GenericCardItem.createItem(itemSplit[1].strip(), Integer.parseInt(itemSplit[0].strip())));
            }
        }
        return result;
    }

    private CollectionType createCollectionType(String collectionType) {
        final CollectionType result = CollectionType.getCollectionTypeByCode(collectionType);
        if (result != null)
            return result;

        return _leagueService.getCollectionTypeByCode(collectionType);
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
        responseWriter.writeHtmlResponse("OK");
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

    private void addSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException, NumberFormatException {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = createNewSoloDraftLeague(parameters);
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
        int cost = Integer.parseInt(parameters.get("cost"));

        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"),
                leagueData.getClass().getName(), parameters.get("serializedParams"), leagueStart, displayEnd);
        _leagueService.clearCache();

        responseWriter.writeHtmlResponse("OK");
    }

    private LeagueData createNewSoloDraftLeague(Map<String, String> parameters) {
        return new SoloDraftLeagueData(_cardBlueprintLibrary,  _formatLibrary,
                _serverObjects.getSoloDraftDefinitions(), parameters.get("serializedParams"));
    }

    private void previewSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException, ParserConfigurationException {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = createNewSoloDraftLeague(parameters);
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private Map<String,String> getSoloDraftOrSealedLeagueParameters(HttpRequest request)
            throws HttpProcessingException, IOException {
        validateLeagueAdmin(request);
        String[] parameterNames = {"format", "start", "seriesDuration", "maxMatches", "name", "cost"};
        Map<String, String> parameterMap = new HashMap<>();
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);

        try {
            for (String parameterName : parameterNames) {
                String value = getFormParameterSafely(postDecoder, parameterName);
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
            logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, request.uri(), ex);
            throw new HttpProcessingException(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        } finally {
            postDecoder.destroy();
        }
    }

    private Map<String,String> getConstructedLeagueParameters(HttpRequest request)
            throws HttpProcessingException, IOException {
        validateLeagueAdmin(request);
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String start = getFormParameterSafely(postDecoder, "start");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");
            String prizeMultiplier = getFormParameterSafely(postDecoder, "prizeMultiplier");
            List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
            List<String> seriesDurations = getFormMultipleParametersSafely(postDecoder, "seriesDuration[]");
            List<String> maxMatches = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

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
        } finally {
            postDecoder.destroy();
        }
    }

    private String serializeSealedLeagueParameters(Map<String,String> parameters) {
        return String.join(",", _formatLibrary.GetSealedTemplate(parameters.get("format")).GetID(),
                parameters.get("start"), parameters.get("seriesDuration"), parameters.get("maxMatches"),
                parameters.get("code"), parameters.get("name"));
    }

    private void addSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        int cost = Integer.parseInt(parameters.get("cost"));
        String serializedParameters = serializeSealedLeagueParameters(parameters);
        LeagueData leagueData = new NewSealedLeagueData(_cardBlueprintLibrary, _formatLibrary, serializedParameters);
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.getFirst().getStart();
        int displayEnd = DateUtils.offsetDate(series.getLast().getEnd(), 2);
        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                serializedParameters, leagueStart, displayEnd);
        _leagueService.clearCache();
        responseWriter.writeHtmlResponse("OK");
    }

    private void getDailyMessage(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String dailyMessage = _hallServer.getDailyMessage();
            if(dailyMessage != null)
                responseWriter.writeJsonResponse(dailyMessage.replace("\n", "<br>"));
        } finally {
            postDecoder.destroy();
        }
    }

    private void setDailyMessage(HttpRequest request, ResponseWriter responseWriter)
            throws HttpProcessingException, IOException {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            _hallServer.setDailyMessage(getFormParameterSafely(postDecoder, "messageOfTheDay"));
            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void shutdown(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            boolean shutdown = Boolean.parseBoolean(getFormParameterSafely(postDecoder, "shutdown"));

            _hallServer.setShutdown(shutdown);

            responseWriter.writeHtmlResponse("OK");
        } catch (Exception e) {
            LOGGER.error("Error response for {}", request.uri(), e);
            responseWriter.writeHtmlResponse("Error handling request");
        } finally {
            postDecoder.destroy();
        }
    }

    private void reloadCards(HttpMessage request, ResponseWriter responseWriter)
            throws HttpProcessingException, InterruptedException {
        validateAdmin(request);

        ChatServer chatServer = _serverObjects.getChatServer();
        chatServer.sendSystemMessageToAllUsers(
                "Server is reloading card definitions.  This will impact game speed until it is complete.");

        Thread.sleep(CARD_LOAD_SLEEP_TIME);
        _cardBlueprintLibrary.reloadAllDefinitions();

        _serverObjects.getProductLibrary().ReloadPacks();

        _formatLibrary.ReloadFormats();
        _formatLibrary.ReloadSealedTemplates();

        chatServer.sendSystemMessageToAllUsers(
                "Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page " +
                        "and please let the mod team know in the game hall ASAP if the problem doesn't go away.");

        responseWriter.writeHtmlResponse("OK");
    }

    private void clearCache(HttpMessage request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        _leagueService.clearCache();
        _serverObjects.getTournamentService().clearCache();

        int before = _cacheManager.getTotalCount();

        _cacheManager.clearCaches();

        int after = _cacheManager.getTotalCount();

        responseWriter.writeHtmlResponse("Before: " + before + "<br><br>After: " + after);
    }

    private void validateAdmin(HttpMessage request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);
        if (!player.hasType(User.Type.ADMIN))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

    private void validateLeagueAdmin(HttpMessage request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);
        if (!player.hasType(User.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }
}