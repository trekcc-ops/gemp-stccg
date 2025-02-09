package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

class DefaultServerRequestHandler {
    final CardBlueprintLibrary _cardBlueprintLibrary;
    final PlayerDAO _playerDao;
    private final LoggedUserHolder _loggedUserHolder;
    final CollectionsManager _collectionsManager;
    final GameHistoryService _gameHistoryService;
    final ServerObjects _serverObjects;
    protected final ObjectMapper _jsonMapper = new ObjectMapper();

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
        _collectionsManager = objects.getCollectionsManager();
        _cardBlueprintLibrary = objects.getCardBlueprintLibrary();
        _gameHistoryService = objects.getGameHistoryService();
    }

    private static boolean isTest() {
        return Boolean.parseBoolean(System.getProperty("test"));
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
        return getFormMultipleParametersSafely(postRequestDecoder,"login[]");
    }


    static Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    protected User getUserIdFromCookiesOrUri(HttpRequest request) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, FormParameter.participantId);
        return getResourceOwnerSafely(request, participantId);
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




}