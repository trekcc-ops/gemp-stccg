package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.cache.CacheManager;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.LeagueDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.User;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.*;
import com.gempukku.stccg.packs.ProductLibrary;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final CacheManager _cacheManager;
    private final HallServer _hallServer;
    private final FormatLibrary _formatLibrary;
    private final LeagueDAO _leagueDao;
    private final CollectionsManager _collectionManager;
    private final PlayerDAO _playerDAO;
    private final AdminService _adminService;
    private final ChatServer _chatServer;

    private static final Logger LOGGER = LogManager.getLogger(AdminRequestHandler.class);

    public AdminRequestHandler(Map<Type, Object> context) {
        super(context);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _tournamentService = extractObject(context, TournamentService.class);
        _cacheManager = extractObject(context, CacheManager.class);
        _hallServer = extractObject(context, HallServer.class);
        _formatLibrary = extractObject(context, FormatLibrary.class);
        _leagueDao = extractObject(context, LeagueDAO.class);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _adminService = extractObject(context, AdminService.class);
        _cardLibrary = extractObject(context, CardBlueprintLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _chatServer = extractObject(context, ChatServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/clearCache") && request.method() == HttpMethod.POST) {
            clearCache(request, responseWriter);
        } else if (uri.equals("/shutdown") && request.method() == HttpMethod.POST) {
            shutdown(request, responseWriter);
        } else if (uri.equals("/reloadCards") && request.method() == HttpMethod.POST) {
            reloadCards(request, responseWriter);
        } else if (uri.equals("/getMOTD") && request.method() == HttpMethod.GET) {
            getMotd(request, responseWriter);
        }else if (uri.equals("/setMOTD") && request.method() == HttpMethod.POST) {
            setMotd(request, responseWriter);
        }else if (uri.equals("/previewSealedLeague") && request.method() == HttpMethod.POST) {
            previewSealedLeague(request, responseWriter);
        } else if (uri.equals("/addSealedLeague") && request.method() == HttpMethod.POST) {
            addSealedLeague(request, responseWriter);
        } else if (uri.equals("/previewConstructedLeague") && request.method() == HttpMethod.POST) {
            previewConstructedLeague(request, responseWriter);
        } else if (uri.equals("/addConstructedLeague") && request.method() == HttpMethod.POST) {
            addConstructedLeague(request, responseWriter);
        } else if (uri.equals("/previewSoloDraftLeague") && request.method() == HttpMethod.POST) {
            previewSoloDraftLeague(request, responseWriter);
        } else if (uri.equals("/addSoloDraftLeague") && request.method() == HttpMethod.POST) {
            addSoloDraftLeague(request, responseWriter);
        } else if (uri.equals("/addItems") && request.method() == HttpMethod.POST) {
            addItems(request, responseWriter);
        } else if (uri.equals("/addItemsToCollection") && request.method() == HttpMethod.POST) {
            addItemsToCollection(request, responseWriter);
        } else if (uri.equals("/banUser") && request.method() == HttpMethod.POST) {
            banUser(request, responseWriter);
        } else if (uri.equals("/resetUserPassword") && request.method() == HttpMethod.POST) {
            resetUserPassword(request, responseWriter);
        } else if (uri.equals("/banMultiple") && request.method() == HttpMethod.POST) {
            banMultiple(request, responseWriter);
        } else if (uri.equals("/banUserTemp") && request.method() == HttpMethod.POST) {
            banUserTemp(request, responseWriter);
        } else if (uri.equals("/unBanUser") && request.method() == HttpMethod.POST) {
            unBanUser(request, responseWriter);
        } else if (uri.equals("/findMultipleAccounts") && request.method() == HttpMethod.POST) {
            findMultipleAccounts(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login").trim();

            List<User> similarPlayers = _playerDAO.findSimilarAccounts(login);
            if (similarPlayers == null)
                throw new HttpProcessingException(400);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
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

    private String getStatus(User similarPlayer) {
        if (similarPlayer.getType().equals(""))
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

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.resetUserPassword(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.banUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            List<String> logins = getFormParametersSafely(postDecoder);
            if (logins == null)
                throw new HttpProcessingException(400);

            for (String login : logins) {
                if (!_adminService.banUser(login))
                    throw new HttpProcessingException(404);
        }

        responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUserTemp(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");
            int duration = Integer.parseInt(getFormParameterSafely(postDecoder, "duration"));

            if (!_adminService.banUserTemp(login, duration))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void unBanUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (!_adminService.unBanUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItemsToCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String reason = getFormParameterSafely(postDecoder, "reason");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<CardCollection.Item> productItems = getProductItems(product);

            Map<User, CardCollection> playersCollection = _collectionManager.getPlayersCollection(collectionType);

            for (Map.Entry<User, CardCollection> playerCollection : playersCollection.entrySet())
                _collectionManager.addItemsToPlayerCollection(true, reason, playerCollection.getKey(), createCollectionType(collectionType), productItems);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItems(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String players = getFormParameterSafely(postDecoder, "players");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<CardCollection.Item> productItems = getProductItems(product);

            List<String> playerNames = getItems(players);

            for (String playerName : playerNames) {
                User player = _playerDao.getPlayer(playerName);

            _collectionManager.addItemsToPlayerCollection(true, "Administrator action", player, createCollectionType(collectionType), productItems);
        }

        responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private List<String> getItems(String values) {
        List<String> result = new LinkedList<>();
        for (String pack : values.split("\n")) {
            String blueprint = pack.trim();
            if (blueprint.length() > 0)
                result.add(blueprint);
        }
        return result;
    }

    private Collection<CardCollection.Item> getProductItems(String values) {
        List<CardCollection.Item> result = new LinkedList<>();
        for (String item : values.split("\n")) {
            item = item.trim();
            if (item.length() > 0) {
                final String[] itemSplit = item.split("x", 2);
                if (itemSplit.length != 2)
                    throw new RuntimeException("Unable to parse the items");
                result.add(CardCollection.Item.createItem(itemSplit[1].trim(), Integer.parseInt(itemSplit[0].trim())));
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
                new NewConstructedLeagueData(_cardLibrary, _formatLibrary, parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.get(0).getStart();
        int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);

        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                parameters.get("serializedParams"), leagueStart, displayEnd);
        _leagueService.clearCache();
        responseWriter.writeHtmlResponse("OK");
    }

    private void previewConstructedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String,String> parameters = getConstructedLeagueParameters(request);
        LeagueData leagueData =
                new NewConstructedLeagueData(_cardLibrary, _formatLibrary, parameters.get("serializedParams"));
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private void writeLeagueDocument(ResponseWriter responseWriter, LeagueData leagueData,
                                     Map<String, String> leagueParameters)
            throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();

        int cost = Integer.parseInt(leagueParameters.get("cost"));
        Element leagueElem = doc.createElement("league");
        final List<LeagueSeriesData> allSeries = leagueData.getSeries();
        int end = allSeries.get(allSeries.size() - 1).getEnd();

        leagueElem.setAttribute("name", leagueParameters.get("name"));
        leagueElem.setAttribute("cost", String.valueOf(cost));
        leagueElem.setAttribute("start", String.valueOf(allSeries.get(0).getStart()));
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

    private void addSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions,
                parameters.get("serializedParams"));
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.get(0).getStart();
        int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);
        int cost = Integer.parseInt(parameters.get("cost"));

        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"),
                leagueData.getClass().getName(), parameters.get("serializedParams"), leagueStart, displayEnd);
        _leagueService.clearCache();

        responseWriter.writeHtmlResponse("OK");
    }

    private void previewSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String, String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new SoloDraftLeagueData(_cardLibrary,  _formatLibrary, _soloDraftDefinitions,
                parameters.get("serializedParams"));
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private Map<String,String> getSoloDraftOrSealedLeagueParameters(HttpRequest request) throws HttpProcessingException, IOException {
        validateLeagueAdmin(request);
        String[] parameterNames = {"format", "start", "seriesDuration", "maxMatches", "name", "cost"};
        Map<String, String> parameterMap = new HashMap<>();
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);

        try {
            for (String parameterName : parameterNames) {
                String value = getFormParameterSafely(postDecoder, parameterName);
                if (value == null || value.trim().isEmpty())
                    throw new HttpProcessingException(400);
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
            logHttpError(LOGGER, 500, request.uri(), ex);
            throw new HttpProcessingException(500);
        } finally {
            postDecoder.destroy();
        }
    }

    private Map<String,String> getConstructedLeagueParameters(HttpRequest request) throws HttpProcessingException, IOException {
        validateLeagueAdmin(request);
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
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
                throw new HttpProcessingException(400);
            }

            if(formats.size() != seriesDurations.size() || formats.size() != maxMatches.size())
                throw new HttpProcessingException(400);

            //The 1 is a hard-coded maximum number of player matches per league.
            //TODO: Get this put into the UI properly.
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
            logHttpError(LOGGER, 500, request.uri(), ex);
            throw new HttpProcessingException(500);
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
        LeagueData leagueData = new NewSealedLeagueData(_cardLibrary, _formatLibrary, serializedParameters);
        List<LeagueSeriesData> series = leagueData.getSeries();
        int leagueStart = series.get(0).getStart();
        int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);
        _leagueDao.addLeague(cost, parameters.get("name"), parameters.get("code"), leagueData.getClass().getName(),
                serializedParameters, leagueStart, displayEnd);
        _leagueService.clearCache();
        responseWriter.writeHtmlResponse("OK");
    }

    private void previewSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        Map<String,String> parameters = getSoloDraftOrSealedLeagueParameters(request);
        LeagueData leagueData = new NewSealedLeagueData(_cardLibrary, _formatLibrary,
                parameters.get("serializedParameters"));
        writeLeagueDocument(responseWriter, leagueData, parameters);
    }

    private void getMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = _hallServer.getMOTD();

            if(motd != null) {
                responseWriter.writeJsonResponse(motd.replace("\n", "<br>"));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void setMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = getFormParameterSafely(postDecoder, "motd");

            _hallServer.setMOTD(motd);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void shutdown(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            boolean shutdown = Boolean.parseBoolean(getFormParameterSafely(postDecoder, "shutdown"));

            _hallServer.setShutdown(shutdown);

            responseWriter.writeHtmlResponse("OK");
        } catch (Exception e) {
            LOGGER.error("Error response for " + request.uri(), e);
            responseWriter.writeHtmlResponse("Error handling request");
        } finally {
            postDecoder.destroy();
        }
    }

    private void reloadCards(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, InterruptedException {
        validateAdmin(request);

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Server is reloading card definitions.  This will impact game speed until it is complete.");

        Thread.sleep(6000);
        _cardLibrary.reloadAllDefinitions();

        _productLibrary.ReloadPacks();

        _formatLibrary.ReloadFormats();
        _formatLibrary.ReloadSealedTemplates();

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page and please let the mod team know in the game hall ASAP if the problem doesn't go away.");

        responseWriter.writeHtmlResponse("OK");
    }

    private void clearCache(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        _leagueService.clearCache();
        _tournamentService.clearCache();

        int before = _cacheManager.getTotalCount();

        _cacheManager.clearCaches();

        int after = _cacheManager.getTotalCount();

        responseWriter.writeHtmlResponse("Before: " + before + "<br><br>After: " + after);
    }

    private void validateAdmin(HttpRequest request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);

        if (!player.hasType(User.Type.ADMIN))
            throw new HttpProcessingException(403);
    }

    private void validateLeagueAdmin(HttpRequest request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);

        if (!player.hasType(User.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(403);
    }
}
