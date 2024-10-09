package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.*;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Type;
import java.util.*;


public class HallRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(HallRequestHandler.class);
    private final CollectionsManager _collectionManager;
    private final FormatLibrary _formatLibrary;
    private final HallServer _hallServer;
    private final LeagueService _leagueService;
    private final CardBlueprintLibrary _library;
    private final LongPollingSystem longPollingSystem;

    public HallRequestHandler(Map<Type, Object> context, LongPollingSystem longPollingSystem) {
        super(context);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _formatLibrary = extractObject(context, FormatLibrary.class);
        _hallServer = extractObject(context, HallServer.class);
        _leagueService = extractObject(context, LeagueService.class);
        _library = extractObject(context, CardBlueprintLibrary.class);
        extractObject(context, GameServer.class);
        this.longPollingSystem = longPollingSystem;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context,
                              ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getHall(request, responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            createTable(request, responseWriter);
        } else if (uri.equals("/update") && request.method() == HttpMethod.POST) {
            updateHall(request, responseWriter);
        } else if (uri.equals("/formats/html") && request.method() == HttpMethod.GET) {
            getFormats(responseWriter);
        } else if (uri.equals("/errata/json") && request.method() == HttpMethod.GET) {
            getErrataInfo(responseWriter);
        } else if (uri.startsWith("/format/") && request.method() == HttpMethod.GET) {
            getFormat(uri.substring(8), responseWriter);
        } else if (uri.startsWith("/queue/") && request.method() == HttpMethod.POST) {
            if (uri.endsWith("/leave")) {
                leaveQueue(request, uri.substring(7, uri.length() - 6), responseWriter);
            } else {
                joinQueue(request, uri.substring(7), responseWriter);
            }
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            dropFromTournament(request, uri.substring(12, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            leaveTable(request, uri.substring(1, uri.length() - 6), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinTable(request, uri.substring(1), responseWriter);
        } else {
            responseWriter.writeError(404);
        }
    }

    private void joinTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        String deckName = getFormParameterSafely(postDecoder, "deckName");
        LOGGER.debug("HallRequestHandler - calling joinTableAsPlayer function from JoinTable");

        try {
            _hallServer.joinTableAsPlayer(tableId, resourceOwner, deckName);
            responseWriter.writeXmlResponse(null);
        } catch (HallException e) {
            try {
                //Try again assuming it's a new player using the default deck library decks
                User libraryOwner = _playerDao.getPlayer("Librarian");
                _hallServer.joinTableAsPlayerWithSpoofedDeck(tableId, resourceOwner, libraryOwner, deckName);
                responseWriter.writeXmlResponse(null);
                return;
            } catch (HallException ex) {
                if(doNotIgnoreError(ex)) {
                    LOGGER.error("Error response for " + request.uri(), ex);
                }
            }
            catch (Exception ex) {
                LOGGER.error("Additional error response for " + request.uri(), ex);
                throw ex;
            }
            responseWriter.writeXmlResponse(marshalException(e));
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private void leaveTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        _hallServer.leaveAwaitingTable(resourceOwner, tableId);
        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void createTable(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            String format = getFormParameterSafely(postDecoder, "format");
            String deckName = getFormParameterSafely(postDecoder, "deckName");
            String timer = getFormParameterSafely(postDecoder, "timer");
            String desc = getFormParameterSafely(postDecoder, "desc").trim();
            String isPrivateVal = getFormParameterSafely(postDecoder, "isPrivate");
            boolean isPrivate = (Boolean.parseBoolean(isPrivateVal));
            String isInviteOnlyVal = getFormParameterSafely(postDecoder, "isInviteOnly");
            boolean isInviteOnly = (Boolean.parseBoolean(isInviteOnlyVal));
            //To prevent annoyance, super long glacial games are hidden from everyone except
            // the participants and admins.
            boolean isHidden = timer.toLowerCase().equals(GameTimer.GLACIAL_TIMER.name());

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            if(isInviteOnly) {
                if(desc.isEmpty()) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Invite-only games must have your intended opponent in the description")));
                    return;
                }

                if(desc.equalsIgnoreCase(resourceOwner.getName())) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Absolutely no playing with yourself!!  Private matches must be with someone else.")));
                    return;
                }

                try {
                    var player = _playerDao.getPlayer(desc);
                    if(player == null)
                    {
                        responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and capitalization and ensure it is exact.")));
                        return;
                    }
                }
                catch(RuntimeException ex) {
                    responseWriter.writeXmlResponse(marshalException(new HallException("Cannot find player '" + desc + "'. Check your spelling and capitalization and ensure it is exact.")));
                    return;
                }
            }



            try {
                _hallServer.createNewTable(format, resourceOwner, deckName, timer, desc, isInviteOnly, isPrivate, isHidden);
                responseWriter.writeXmlResponse(null);
            }
            catch (HallException e) {
                try
                {
                    //try again assuming it's a new player with one of the default library decks selected
                    _hallServer.createNewTable(format, resourceOwner, _playerDao.getPlayer("Librarian"),
                            deckName, timer, "(New Player) " + desc, isInviteOnly, isPrivate, isHidden);
                    responseWriter.writeXmlResponse(null);
                    return;
                }
                catch (HallException ignored) { }

                responseWriter.writeXmlResponse(marshalException(e));
            }
        }
        catch (Exception ex)
        {
            //This is a worthless error that doesn't need to be spammed into the log
            if(doNotIgnoreError(ex)) {
                LOGGER.error("Error response for " + request.uri(), ex);
            }
            responseWriter.writeXmlResponse(marshalException(new HallException("Failed to create table. Please try again later.")));
        }
        finally {
            postDecoder.destroy();
        }
    }



    private boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

    private void dropFromTournament(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        _hallServer.dropFromTournament(tournamentId, resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void joinQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        String deckName = getFormParameterSafely(postDecoder, "deckName");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        try {
            _hallServer.joinQueue(queueId, resourceOwner, deckName);
            responseWriter.writeXmlResponse(null);
        } catch (HallException e) {
            if(doNotIgnoreError(e)) {
                LOGGER.error("Error response for " + request.uri(), e);
            }
            responseWriter.writeXmlResponse(marshalException(e));
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private void leaveQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        _hallServer.leaveQueue(queueId, resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private Document marshalException(HallException e) throws ParserConfigurationException {
        Document doc = createNewDoc();
        Element error = doc.createElement("error");
        error.setAttribute("message", e.getMessage());
        doc.appendChild(error);
        return doc;
    }

    private void getFormat(String format, ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        GameFormat gameFormat = _formatLibrary.getFormat(format);
        appendFormat(result, gameFormat);

        responseWriter.writeHtmlResponse(result.toString());
    }

    private void getFormats(ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        for (GameFormat gameFormat : _formatLibrary.getHallFormats().values()) {
            appendFormat(result, gameFormat);
        }

        responseWriter.writeHtmlResponse(result.toString());
    }

    private void appendFormat(StringBuilder result, GameFormat gameFormat) throws CardNotFoundException {
        result.append("<b>").append(gameFormat.getName()).append("</b>");
        result.append("<ul>");
        result.append("<li>valid sets: ");
        for (Integer integer : gameFormat.getValidSetNums())
            result.append(integer).append(", ");
        result.append("</li>");
        if (!gameFormat.getBannedCards().isEmpty()) {
            result.append("<li>Banned cards (can't be played): ");
            appendCards(result, gameFormat.getBannedCards());
            result.append("</li>");
        }
        if (!gameFormat.getRestrictedCardNames().isEmpty()) {
            result.append("<li>Restricted by card name: ");
            boolean first = true;
            for (String cardName : gameFormat.getRestrictedCardNames()) {
                if (!first)
                    result.append(", ");
                result.append(cardName);
                first = false;
            }
            result.append("</li>");
        }
        if (!gameFormat.getErrataCardMap().isEmpty()) {
            result.append("<li>Errata: ");
            appendCards(result, new ArrayList<>(new LinkedHashSet<>(gameFormat.getErrataCardMap().values())));
            result.append("</li>");
        }
        if (!gameFormat.getValidCards().isEmpty()) {
            result.append("<li>Additional valid: ");
            List<String> additionalValidCards = gameFormat.getValidCards();
            appendCards(result, additionalValidCards);
            result.append("</li>");
        }
        result.append("</ul>");
    }

    private void appendCards(StringBuilder result, List<String> additionalValidCards) throws CardNotFoundException {
        if (!additionalValidCards.isEmpty()) {
            for (String blueprintId : additionalValidCards)
                result.append(_library.getCardBlueprint(blueprintId).getCardLink()).append(", ");
            if (additionalValidCards.isEmpty())
                result.append("none,");
        }
    }

    private void getErrataInfo(ResponseWriter responseWriter) throws JsonProcessingException {
        responseWriter.writeJsonResponse(JsonUtils.toJsonString(_library.getErrata()));
    }

    private void getHall(HttpRequest request, ResponseWriter responseWriter) {
        try {
            User resourceOwner = getResourceOwner(request);
            Document doc = createNewDoc();
            User player = getResourceOwnerSafely(request, null);

            Element hall = doc.createElement("hall");
            hall.setAttribute("currency", String.valueOf(_collectionManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));

            _hallServer.signupUserForHall(resourceOwner, new SerializeHallInfoVisitor(doc, hall));
            for (Map.Entry<String, GameFormat> format : _formatLibrary.getHallFormats().entrySet()) {
                //playtest formats are opt-in
                if (format.getKey().startsWith("test") && !player.getType().contains("p"))
                    continue;

                Element formatElem = doc.createElement("format");
                formatElem.setAttribute("type", format.getKey());
                formatElem.appendChild(doc.createTextNode(format.getValue().getName()));
                hall.appendChild(formatElem);
            }
            for (League league : _leagueService.getActiveLeagues()) {
                final LeagueSeriesData seriesData = _leagueService.getCurrentLeagueSeries(league);
                if (seriesData != null && _leagueService.isPlayerInLeague(league, resourceOwner)) {
                    Element formatElem = doc.createElement("format");
                    formatElem.setAttribute("type", league.getType());
                    formatElem.appendChild(doc.createTextNode(league.getName()));
                    hall.appendChild(formatElem);
                }
            }
            doc.appendChild(hall);
            responseWriter.writeXmlResponse(doc);
        } catch (HttpProcessingException exp) {
            logHttpError(LOGGER, exp.getStatus(), request.uri(), exp);
            responseWriter.writeError(exp.getStatus());
        } catch (Exception exp) {
            LOGGER.error("Error response for " + request.uri(), exp);
            responseWriter.writeError(500);
        }
    }

    private void updateHall(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String participantId = getFormParameterSafely(postDecoder, "participantId");
            int channelNumber = Integer.parseInt(getFormParameterSafely(postDecoder, "channelNumber"));

            User resourceOwner = getResourceOwnerSafely(request, participantId);
            processLoginReward(resourceOwner.getName());

            try {
                HallCommunicationChannel commChannel = _hallServer.getCommunicationChannel(resourceOwner, channelNumber);
                HallUpdateLongPollingResource polledResource =
                        new HallUpdateLongPollingResource(commChannel, request, resourceOwner, responseWriter);
                longPollingSystem.processLongPollingResource(polledResource, commChannel);
            }
            catch (SubscriptionExpiredException exp) {
                logHttpError(LOGGER, 410, request.uri(), exp);
                responseWriter.writeError(410);
            }
            catch (SubscriptionConflictException exp) {
                logHttpError(LOGGER, 409, request.uri(), exp);
                responseWriter.writeError(409);
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private class HallUpdateLongPollingResource implements LongPollingResource {
        private final HttpRequest _request;
        private final HallCommunicationChannel _hallCommunicationChannel;
        private final User _resourceOwner;
        private final ResponseWriter _responseWriter;
        private boolean _processed;

        private HallUpdateLongPollingResource(HallCommunicationChannel hallCommunicationChannel, HttpRequest request, User resourceOwner, ResponseWriter responseWriter) {
            _hallCommunicationChannel = hallCommunicationChannel;
            _request = request;
            _resourceOwner = resourceOwner;
            _responseWriter = responseWriter;
        }

        @Override
        public synchronized boolean wasProcessed() {
            return _processed;
        }

        @Override
        public synchronized void processIfNotProcessed() {
            if (!_processed) {
                try {
                    Document doc = createNewDoc();

                    Element hall = doc.createElement("hall");
                    _hallCommunicationChannel.processCommunicationChannel(_hallServer, _resourceOwner, new SerializeHallInfoVisitor(doc, hall));
                    hall.setAttribute("currency", String.valueOf(_collectionManager.getPlayerCollection(_resourceOwner, CollectionType.MY_CARDS.getCode()).getCurrency()));

                    doc.appendChild(hall);

                    Map<String, String> headers = new HashMap<>();
                    processDeliveryServiceNotification(_request, headers);

                    _responseWriter.writeXmlResponse(doc, headers);
                } catch (Exception exp) {
                    logHttpError(LOGGER, 500, _request.uri(), exp);
                    _responseWriter.writeError(500);
                }
                _processed = true;
            }
        }
    }

    private static class SerializeHallInfoVisitor implements HallChannelVisitor {
        private final Document _doc;
        private final Element _hall;

        public SerializeHallInfoVisitor(Document doc, Element hall) {
            _doc = doc;
            _hall = hall;
        }

        @Override
        public void channelNumber(int channelNumber) {
            _hall.setAttribute("channelNumber", String.valueOf(channelNumber));
        }

        @Override
        public void newPlayerGame(String gameId) {
            Element newGame = _doc.createElement("newGame");
            newGame.setAttribute("id", gameId);
            _hall.appendChild(newGame);
        }

        @Override
        public void serverTime(String serverTime) {
            _hall.setAttribute("serverTime", serverTime);
        }

        @Override
        public void changedDailyMessage(String message) {
            _hall.setAttribute("messageOfTheDay", message);
        }

        @Override
        public void addTournamentQueue(String queueId, Map<String, String> props) {
            appendElementWithProperties("queue", queueId, props, "add");
        }

        @Override
        public void updateTournamentQueue(String queueId, Map<String, String> props) {
            appendElementWithProperties("queue", queueId, props, "update");
        }

        @Override
        public void removeTournamentQueue(String queueId) {
            appendRemoveElement("queue", queueId);
        }

        @Override
        public void addTournament(String tournamentId, Map<String, String> props) {
            appendElementWithProperties("tournament", tournamentId, props, "add");
        }

        @Override
        public void updateTournament(String tournamentId, Map<String, String> props) {
            appendElementWithProperties("tournament", tournamentId, props, "update");
        }

        @Override
        public void removeTournament(String tournamentId) {
            appendRemoveElement("tournament", tournamentId);
        }

        @Override
        public void addTable(String tableId, Map<String, String> props) {
            appendElementWithProperties("table", tableId, props, "add");
        }

        @Override
        public void updateTable(String tableId, Map<String, String> props) {
            appendElementWithProperties("table", tableId, props, "update");
        }
        @Override
        public void removeTable(String tableId) {
            appendRemoveElement("table", tableId);
        }

        private void appendElementWithProperties(String tagName, String id, Map<String, String> props, String action) {
            Element elem = _doc.createElement(tagName);
            elem.setAttribute("action", action);
            elem.setAttribute("id", id);
            for (Map.Entry<String, String> attribute : props.entrySet())
                elem.setAttribute(attribute.getKey(), attribute.getValue());
            _hall.appendChild(elem);
        }

        private void appendRemoveElement(String tagName, String id) {
            Element elem = _doc.createElement(tagName);
            elem.setAttribute("action", "remove");
            elem.setAttribute("id", id);
            _hall.appendChild(elem);
        }
    }
}