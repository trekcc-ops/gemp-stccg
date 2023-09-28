package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.DateUtils;
import com.gempukku.lotro.PlayerLock;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.CardNotFoundException;
import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.User;
import com.gempukku.lotro.game.formats.FormatLibrary;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

public class LotroServerRequestHandler {
    protected final CardBlueprintLibrary _library;
    protected final PlayerDAO _playerDao;
    protected final LoggedUserHolder _loggedUserHolder;
    private final TransferDAO _transferDAO;
    private final CollectionsManager _collectionManager;

    private static final Logger _log = Logger.getLogger(LotroServerRequestHandler.class);

    public LotroServerRequestHandler(Map<Type, Object> context) {
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
                    _collectionManager.addCurrencyToPlayerCollection(true, "Singup reward", player, CollectionType.MY_CARDS, 20000);
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

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !participantId.equals("null") && !participantId.equals("")) {
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
        if (parameterValues != null && parameterValues.size() > 0)
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
        List<InterfaceHttpData> datas = postRequestDecoder.getBodyHttpDatas("login[]");
        if (datas == null)
            return null;
        List<String> result = new LinkedList<>();
        for (InterfaceHttpData data : datas) {
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

    protected String generateCardTooltip(LotroCardBlueprint bp, String bpid) {
        String[] parts = bpid.split("_");
        int setnum = Integer.parseInt(parts[0]);
        String set = String.format("%02d", setnum);
        String subset = "S";
        int version = 0;
        if(setnum >= 50 && setnum <= 69) {
            setnum -= 50;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 70 && setnum <= 89) {
            setnum -= 70;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 100 && setnum <= 149) {
            setnum -= 100;
            set = "V" + setnum;
        }
        int cardnum = Integer.parseInt(parts[1].replace("*", "").replace("T", ""));

        String id = "LOTR-EN" + set + subset + String.format("%03d", cardnum) + "." + String.format("%01d", version);

        return "<span class=\"tooltip\">" + GameUtils.getFullName(bp)
                + "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>";
    }

    protected String generateCardTooltip(CardCollection.Item item) throws CardNotFoundException {
        return generateCardTooltip(_library.getLotroCardBlueprint(item.getBlueprintId()), item.getBlueprintId());
    }

    protected String listCard(String label, String card, boolean showToolTip) throws CardNotFoundException {
        if (card == null) {
            return "";
        } else {
            String cardText;
            if (showToolTip)
                cardText = generateCardTooltip(_library.getLotroCardBlueprint(card), card);
            else
                cardText = GameUtils.getFullName(_library.getLotroCardBlueprint(card));
            return "<b>" + label + ":</b> " + cardText + "<br/>";
        }
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
                cardText = GameUtils.getFullName(_library.getLotroCardBlueprint(item.getBlueprintId()));
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
        result.append(listCards("Free Peoples Draw Deck","side:FREE_PEOPLE sort:cardType,culture,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));
        result.append(listCards("Shadow Draw Deck","side:SHADOW sort:cardType,culture,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));

        return result.toString();
    }
}