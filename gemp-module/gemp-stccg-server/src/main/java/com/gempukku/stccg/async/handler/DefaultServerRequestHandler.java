package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.game.SortAndFilterCards;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

public class DefaultServerRequestHandler {
    protected final CardBlueprintLibrary _cardBlueprintLibrary;
    protected final PlayerDAO _playerDao;
    protected final LoggedUserHolder _loggedUserHolder;
    private final TransferDAO _transferDAO;
    protected final CollectionsManager _collectionsManager;
    protected final GameHistoryService _gameHistoryService;

    public DefaultServerRequestHandler(ServerObjects objects) {
        _playerDao = objects.getPlayerDAO();
        _loggedUserHolder = objects.getLoggedUserHolder();
        _transferDAO = objects.getTransferDAO();
        _collectionsManager = objects.getCollectionsManager();
        _cardBlueprintLibrary = objects.getCardBlueprintLibrary();
        _gameHistoryService = objects.getGameHistoryService();
    }

    private boolean isTest() {
        return Boolean.parseBoolean(System.getProperty("test"));
    }

    protected final void processLoginReward(String loggedUser) throws Exception {
        if (loggedUser != null) {
            User player = _playerDao.getPlayer(loggedUser);
            synchronized (player.getName().intern()) {
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
                int latestMonday = DateUtils.getMondayBeforeOrOn(now);

                Integer lastReward = player.getLastLoginReward();
                if (lastReward == null) {
                    _playerDao.setLastReward(player, latestMonday);
                    _collectionsManager.addCurrencyToPlayerCollection(true, "Signup reward", player,
                            CollectionType.MY_CARDS, 20000);
                } else {
                    if (latestMonday != lastReward) {
                        if (_playerDao.updateLastReward(player, lastReward, latestMonday))
                            _collectionsManager.addCurrencyToPlayerCollection(true, "Weekly reward",
                                    player, CollectionType.MY_CARDS, 5000);
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

    protected final User getResourceOwnerSafely(HttpRequest request, String participantId)
            throws HttpProcessingException {
        String loggedUser = getLoggedUser(request);
        if (isTest() && loggedUser == null)
            loggedUser = participantId;

        if (loggedUser == null)
            throw new HttpProcessingException(401);

        User resourceOwner = _playerDao.getPlayer(loggedUser);

        if (resourceOwner == null)
            throw new HttpProcessingException(401);

        if (resourceOwner.hasType(User.Type.ADMIN) && participantId != null && !participantId.equals("null") &&
                !participantId.isEmpty()) {
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
            return parameterValues.getFirst();
        else
            return null;
    }

    protected List<String> getFormMultipleParametersSafely(HttpPostRequestDecoder postRequestDecoder,
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

    protected String getFormParameterSafely(HttpPostRequestDecoder postRequestDecoder, String parameterName)
            throws IOException, HttpPostRequestDecoder.NotEnoughDataDecoderException {
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

    protected List<String> getLoginParametersSafely(HttpPostRequestDecoder postRequestDecoder)
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

    protected Map<String, String> logUserReturningHeaders(String remoteIp, String login) throws SQLException {
        _playerDao.updateLastLoginIp(login, remoteIp);

        String sessionId = _loggedUserHolder.logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }

    @SuppressWarnings("SpellCheckingInspection")
    protected String generateCardTooltip(GenericCardItem item) throws CardNotFoundException {
        String blueprintId = item.getBlueprintId();
        CardBlueprint bp = _cardBlueprintLibrary.getCardBlueprint(blueprintId);
        return "<span class=\"tooltip\">" + bp.getFullName()
                + "<span><img class=\"ttimage\" src=\"" + bp.getImageUrl() + "\"></span></span>";
    }

    protected String listCards(String deckName, String filter, DefaultCardCollection deckCards, boolean countCards,
                               SortAndFilterCards sortAndFilter, FormatLibrary formatLibrary, boolean showToolTip)
            throws CardNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append("<br/><b>").append(deckName).append(":</b><br/>");
        for (GenericCardItem item : sortAndFilter.process(filter, deckCards.getAll(), _cardBlueprintLibrary, formatLibrary)) {
            if (countCards)
                sb.append(item.getCount()).append("x ");
            String cardText = showToolTip?
                    generateCardTooltip(item) : _cardBlueprintLibrary.getCardBlueprint(item.getBlueprintId()).getFullName();
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
            deckCards.addItem(_cardBlueprintLibrary.getBaseBlueprintId(card), 1);

        result.append(listCards("Adventure Deck","cardType:SITE sort:twilight",
                deckCards,false, sortAndFilter, formatLibrary, showToolTip));
        result.append(listCards("Free Peoples Draw Deck","sort:cardType,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));
        result.append(listCards("Shadow Draw Deck","sort:cardType,name",
                deckCards,true, sortAndFilter, formatLibrary, showToolTip));

        return result.toString();
    }

    protected Document createNewDoc() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    protected User getResourceOwner(HttpRequest request) throws HttpProcessingException {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");
        return getResourceOwnerSafely(request, participantId);
    }

}