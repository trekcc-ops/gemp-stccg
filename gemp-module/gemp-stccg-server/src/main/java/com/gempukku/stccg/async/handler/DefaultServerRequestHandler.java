package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.PlayerLock;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.SortAndFilterCards;
import com.gempukku.stccg.game.User;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

public class DefaultServerRequestHandler {
    protected final CardBlueprintLibrary _library;
    protected final PlayerDAO _playerDao;
    protected final LoggedUserHolder _loggedUserHolder;
    private final TransferDAO _transferDAO;
    private final CollectionsManager _collectionManager;

    public DefaultServerRequestHandler(Map<Type, Object> context) {
        _playerDao = extractObject(context, PlayerDAO.class);
        _loggedUserHolder = extractObject(context, LoggedUserHolder.class);
        _transferDAO = extractObject(context, TransferDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _library = extractObject(context, CardBlueprintLibrary.class);
    }

    private boolean isTest() {
        return Boolean.parseBoolean(System.getProperty("test"));
    }

    protected final void processLoginReward(String loggedUser) throws Exception {
        if (loggedUser != null) {
            User player = _playerDao.getPlayer(loggedUser);
            synchronized (PlayerLock.getLock(player)) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
                int latestMonday = DateUtils.getMondayBeforeOrOn(now);

                Integer lastReward = player.getLastLoginReward();
                if (lastReward == null) {
                    _playerDao.setLastReward(player, latestMonday);
                    _collectionManager.addCurrencyToPlayerCollection(true, "Signup reward", player, CollectionType.MY_CARDS, 20000);
                } else {
                    if (latestMonday != lastReward) {
                        if (_playerDao.updateLastReward(player, lastReward, latestMonday))
                            _collectionManager.addCurrencyToPlayerCollection(true, "Weekly reward", player, CollectionType.MY_CARDS, 5000);
                    }
                }
            }
        }
    }

    private String getLoggedUser(HttpRequest request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if (cookie.name().equals("loggedUser")) {
                    String value = cookie.value();
                    if (value != null) {
                        return _loggedUserHolder.getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }

    protected final void processDeliveryServiceNotification(HttpRequest request, Map<String, String> headersToAdd) {
        String logged = getLoggedUser(request);
        if (logged != null && _transferDAO.hasUndeliveredPackages(logged))
            headersToAdd.put("Delivery-Service-Package", "true");
    }

    protected final User getResourceOwnerSafely(HttpRequest request, String participantId) throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);
        if (isTest() && loggedUser == null)
            loggedUser = participantId;

        if (loggedUser == null)
            throw new HttpProcessingException(401);

        User resourceOwner = _playerDao.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(401);

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !participantId.equals("null") && !participantId.isEmpty()) {
            resourceOwner = _playerDao.getPlayer(participantId);
            if (resourceOwner == null)
                throw new HttpProcessingException(401);
        }
        return resourceOwner;
    }

    protected final User getLibrarian() throws HttpProcessingException {
        User resourceOwner = _playerDao.getPlayer("Librarian");

        if (resourceOwner == null)
            throw new HttpProcessingException(401);

        return resourceOwner;
    }

    protected String getQueryParameterSafely(QueryStringDecoder queryStringDecoder, String parameterName) {
        List<String> parameterValues = queryStringDecoder.parameters().get(parameterName);
        if (parameterValues != null && !parameterValues.isEmpty())
            return parameterValues.get(0);
        else
            return null;
    }

    protected List<String> getFormMultipleParametersSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName) throws HttpPostRequestDecoder.NotEnoughDataDecoderException, IOException {
        List<String> result = new LinkedList<>();
        List<InterfaceHttpData> datas = postRequestDecoder.getBodyHttpDatas(parameterName);
        if (datas == null)
            return Collections.emptyList();
        for (InterfaceHttpData data : datas) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute attribute = (Attribute) data;
                result.add(attribute.getValue());
            }

        }
        return result;
    }

    protected String getFormParameterSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName) throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
        InterfaceHttpData data = postRequestDecoder.getBodyHttpData(parameterName);
        if (data == null)
            return null;
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            return attribute.getValue();
        } else {
            return null;
        }
    }

    protected List<String> getFormParametersSafely(HttpPostRequestDecoder postRequestDecoder) throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
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

    protected <T> T extractObject(Map<Type, Object> context, Class<T> clazz) {
        Object value = context.get(clazz);
        return (T) value;
    }

    protected Map<String, String> logUserReturningHeaders(String remoteIp, String login) throws SQLException {
        _playerDao.updateLastLoginIp(login, remoteIp);

        String sessionId = _loggedUserHolder.logUser(login);
        return Collections.singletonMap(SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }

    protected String generateCardTooltip(CardBlueprint bp, String blueprintId) {
        String[] parts = blueprintId.split("_");
        int setNum = Integer.parseInt(parts[0]);
        String set = String.format("%02d", setNum);
        String subset = "S";
        int version = 0;
        if(setNum >= 50 && setNum <= 69) {
            setNum -= 50;
            set = String.format("%02d", setNum);
            subset = "E";
            version = 1;
        }
        else if(setNum >= 70 && setNum <= 89) {
            setNum -= 70;
            set = String.format("%02d", setNum);
            subset = "E";
            version = 1;
        }
        else if(setNum >= 100 && setNum <= 149) {
            setNum -= 100;
            set = "V" + setNum;
        }
        int cardNum = Integer.parseInt(parts[1].replace("*", "").replace("T", ""));

        String id = "LOTR-EN" + set + subset + String.format("%03d", cardNum) + "." + String.format("%01d", version);

        return "<span class=\"tooltip\">" + bp.getFullName()
                + "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>";
    }

    protected String generateCardTooltip(CardCollection.Item item) throws CardNotFoundException {
        return generateCardTooltip(_library.getCardBlueprint(item.getBlueprintId()), item.getBlueprintId());
    }

    protected String listCards(String deckName, String filter, DefaultCardCollection deckCards, boolean countCards,
                               SortAndFilterCards sortAndFilter, FormatLibrary formatLibrary, boolean showToolTip)
            throws CardNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("<br/><b>").append(deckName).append(":</b><br/>");
        for (CardCollection.Item item : sortAndFilter.process(filter, deckCards.getAll(), _library, formatLibrary)) {
            if (countCards)
                sb.append(item.getCount()).append("x ");
            String cardText;
            if (showToolTip)
                cardText = generateCardTooltip(item);
            else
                cardText = _library.getCardBlueprint(item).getFullName();
            sb.append(cardText).append("<br/>");
        }
        return sb.toString();
    }
    
    protected String getHTMLDeck(CardDeck deck, boolean showToolTip, SortAndFilterCards sortAndFilter,
                                 FormatLibrary formatLibrary)
            throws CardNotFoundException {
        
        StringBuilder result = new StringBuilder();

        DefaultCardCollection deckCards = new DefaultCardCollection();
        for (String card : deck.getDrawDeckCards())
            deckCards.addItem(_library.getBaseBlueprintId(card), 1);

        result.append(listCards("Adventure Deck","cardType:SITE sort:siteNumber,twilight",
                deckCards,false, sortAndFilter, formatLibrary, showToolTip));
        result.append(listCards("Free Peoples Draw Deck","side:FREE_PEOPLE sort:cardType,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));
        result.append(listCards("Shadow Draw Deck","side:SHADOW sort:cardType,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));

        return result.toString();
    }
}