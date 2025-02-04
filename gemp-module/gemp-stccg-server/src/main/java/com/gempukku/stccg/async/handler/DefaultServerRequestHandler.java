package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.*;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

class DefaultServerRequestHandler {
    private final static Map.Entry<String, String> HEADERS_TO_ADD =
            new AbstractMap.SimpleEntry<>("Delivery-Service-Package", "true");
    private final static int SIGNUP_REWARD = 20000;
    private final static int WEEKLY_REWARD = 5000;
    final CardBlueprintLibrary _cardBlueprintLibrary;
    final PlayerDAO _playerDao;
    private final LoggedUserHolder _loggedUserHolder;
    private final TransferDAO _transferDAO;
    final CollectionsManager _collectionsManager;
    final GameHistoryService _gameHistoryService;
    final ServerObjects _serverObjects;

    protected enum FormParameter {
        availablePicks, blueprintId, cardId, channelNumber, choiceId, collectionType,
        cost, count, decisionId, decisionValue,
        deck, deckContents, decklist, deckName, decks,
        desc, duration, filter, format, id, includeEvents, isInviteOnly, isPrivate, length, login, maxMatches,
        message, messageOfTheDay, name, notes, oldDeckName,
        ownedMin, pack, participantId, password, players, price, prizeMultiplier, product, reason, selection,
        seriesDuration, shutdown, start, startDay, targetFormat, timer
    }

    DefaultServerRequestHandler(ServerObjects objects) {
        _serverObjects = objects;
        _playerDao = objects.getPlayerDAO();
        _loggedUserHolder = objects.getLoggedUserHolder();
        _transferDAO = objects.getTransferDAO();
        _collectionsManager = objects.getCollectionsManager();
        _cardBlueprintLibrary = objects.getCardBlueprintLibrary();
        _gameHistoryService = objects.getGameHistoryService();
    }

    private static boolean isTest() {
        return Boolean.parseBoolean(System.getProperty("test"));
    }

    final void processLoginReward(String loggedUser) throws Exception {
        if (loggedUser != null) {
            User player = _playerDao.getPlayer(loggedUser);
            synchronized (player.getName().intern()) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
                int latestMonday = DateUtils.getMondayBeforeOrOn(now);

                Integer lastReward = player.getLastLoginReward();
                if (lastReward == null) {
                    _playerDao.setLastReward(player, latestMonday);
                    _collectionsManager.addCurrencyToPlayerCollection(true, "Signup reward", player,
                            CollectionType.MY_CARDS, SIGNUP_REWARD);
                } else {
                    if (latestMonday != lastReward) {
                        if (_playerDao.updateLastReward(player, lastReward, latestMonday))
                            _collectionsManager.addCurrencyToPlayerCollection(true, "Weekly reward",
                                    player, CollectionType.MY_CARDS, WEEKLY_REWARD);
                    }
                }
            }
        }
    }

    private String getLoggedUser(HttpMessage request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("loggedUser".equals(cookie.name())) {
                    String value = cookie.value();
                    if (value != null) {
                        return _loggedUserHolder.getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }

    final void processDeliveryServiceNotification(HttpMessage request,
                                                  Map<? super String, ? super String> headersToAdd) {
        String logged = getLoggedUser(request);
        if (logged != null && _transferDAO.hasUndeliveredPackages(logged))
            headersToAdd.put(HEADERS_TO_ADD.getKey(), HEADERS_TO_ADD.getValue());
    }

    final User getResourceOwnerSafely(HttpMessage request, String participantId)
            throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);
        if (isTest() && loggedUser == null)
            loggedUser = participantId;

        if (loggedUser == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        User resourceOwner = _playerDao.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !"null".equals(participantId) &&
                !participantId.isEmpty()) {
            resourceOwner = _playerDao.getPlayer(participantId);
            if (resourceOwner == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
        return resourceOwner;
    }

    final User getResourceOwnerSafely(HttpMessage request)
            throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);

        if (loggedUser == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        User resourceOwner = _playerDao.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        return resourceOwner;
    }


    final User getLibrarian() throws HttpProcessingException {
        User resourceOwner = _playerDao.getPlayer("Librarian");

        if (resourceOwner == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401

        return resourceOwner;
    }

    static String getQueryParameterSafely(QueryStringDecoder decoder, FormParameter parameter) {
        List<String> parameterValues = decoder.parameters().get(parameter.name());
        if (parameterValues != null && !parameterValues.isEmpty())
            return parameterValues.getFirst();
        else
            return null;
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

    static List<String> getLoginParametersSafely(InterfaceHttpPostRequestDecoder postRequestDecoder)
            throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        List<InterfaceHttpData> httpData = postRequestDecoder.getBodyHttpDatas("login[]");
        if (httpData == null)
            return null;
        List<String> result = new LinkedList<>();
        for (InterfaceHttpData data : httpData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                result.add(attribute.getValue());
            }
        }
        return result;
    }

    final Map<String, String> logUserReturningHeaders(String remoteIp, String login) throws SQLException {
        _playerDao.updateLastLoginIp(login, remoteIp);

        String sessionId = _loggedUserHolder.logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }


    static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    final User getResourceOwner(HttpRequest request) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        return getResourceOwnerSafely(request, participantId);
    }

}