package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.game.ParticipantCommunicationVisitor;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final GameServer _gameServer;
    private final LongPollingSystem longPollingSystem;
    private final Set<Phase> _autoPassDefault = new HashSet<>();

    private static final Logger LOGGER = LogManager.getLogger(GameRequestHandler.class);

    public GameRequestHandler(ServerObjects objects, LongPollingSystem longPollingSystem) {
        super(objects);
        _gameServer = objects.getGameServer();
        this.longPollingSystem = longPollingSystem;

        _autoPassDefault.add(Phase.FELLOWSHIP);
        _autoPassDefault.add(Phase.MANEUVER);
        _autoPassDefault.add(Phase.ARCHERY);
        _autoPassDefault.add(Phase.ASSIGNMENT);
        _autoPassDefault.add(Phase.REGROUP);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.startsWith("/") && uri.endsWith("/cardInfo") && request.method() == HttpMethod.GET) {
            getCardInfo(request, uri.substring(1, uri.length() - 9), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/concede") && request.method() == HttpMethod.POST) {
            concede(request, uri.substring(1, uri.length() - 8), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/cancel") && request.method() == HttpMethod.POST) {
            cancel(request, uri.substring(1, uri.length() - 7), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getGameState(request, uri.substring(1), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            updateGameState(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void updateGameState(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        int channelNumber = Integer.parseInt(getFormParameterSafely(postDecoder, "channelNumber"));
        Integer decisionId = null;
        String decisionIdStr = getFormParameterSafely(postDecoder, "decisionId");
        if (decisionIdStr != null)
            decisionId = Integer.parseInt(decisionIdStr);
        String decisionValue = getFormParameterSafely(postDecoder, "decisionValue");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardGameMediator gameMediator = _gameServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), getAutoPassPhases(request));

        try {
            if (decisionId != null)
                gameMediator.playerAnsweredNew(resourceOwner, channelNumber, decisionId, decisionValue);

            GameCommunicationChannel commChannel = gameMediator.getCommunicationChannel(resourceOwner, channelNumber);
            LongPollingResource pollingResource =
                    new GameUpdateLongPollingResource(commChannel, channelNumber, gameMediator, responseWriter);
            longPollingSystem.processLongPollingResource(pollingResource, commChannel);
        } catch (SubscriptionConflictException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
        } catch (PrivateInformationException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        } catch (SubscriptionExpiredException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_GONE); // 410
        }
        } finally {
            postDecoder.destroy();
        }
    }

    private class GameUpdateLongPollingResource implements LongPollingResource {
        private final GameCommunicationChannel _gameCommunicationChannel;
        private final CardGameMediator _gameMediator;
        private final int _channelNumber;
        private final ResponseWriter _responseWriter;
        private boolean _processed;

        private GameUpdateLongPollingResource(GameCommunicationChannel commChannel, int channelNumber,
                                              CardGameMediator gameMediator, ResponseWriter responseWriter) {
            _gameCommunicationChannel = commChannel;
            _channelNumber = channelNumber;
            _gameMediator = gameMediator;
            _responseWriter = responseWriter;
        }

        @Override
        public final synchronized boolean wasProcessed() {
            return _processed;
        }

        @Override
        public final synchronized void processIfNotProcessed() {
            if (!_processed) {
                try {
                    Document doc = createNewDoc();
                    Element update = doc.createElement("update");

                    _gameMediator.processVisitor(
                            _gameCommunicationChannel, _channelNumber, new SerializationVisitor(doc, update));

                    doc.appendChild(update);

                    _responseWriter.writeXmlResponse(doc);
                } catch (Exception e) {
                    logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, "game update poller", e);
                    _responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
                }
                _processed = true;
            }
        }
    }

    private void cancel(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");
        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardGameMediator gameMediator = _gameServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        gameMediator.cancel(resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void concede(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
        String participantId = getFormParameterSafely(postDecoder, "participantId");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardGameMediator gameMediator = _gameServer.getGameById(gameId);
        if (gameMediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        gameMediator.concede(resourceOwner);

        responseWriter.writeXmlResponse(null);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getCardInfo(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String cardIdStr = getQueryParameterSafely(queryDecoder, "cardId");
        if (cardIdStr.startsWith("extra")) {
            responseWriter.writeHtmlResponse("");
        } else {
            int cardId = Integer.parseInt(cardIdStr);

            CardGameMediator gameMediator = _gameServer.getGameById(gameId);
            if (gameMediator == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            responseWriter.writeHtmlResponse(gameMediator.produceCardInfo(cardId));
        }
    }

    private void getGameState(HttpRequest request, String gameId, ResponseWriter responseWriter) throws Exception {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        String participantId = getQueryParameterSafely(queryDecoder, "participantId");

        User resourceOwner = getResourceOwnerSafely(request, participantId);

        CardGameMediator gameMediator = _gameServer.getGameById(gameId);

        if (gameMediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), getAutoPassPhases(request));

        Document doc = createNewDoc();
        Element gameState = doc.createElement("gameState");

        try {
            gameMediator.signupUserForGame(resourceOwner, new SerializationVisitor(doc, gameState));
        } catch (PrivateInformationException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }

        doc.appendChild(gameState);

        responseWriter.writeXmlResponse(doc);
    }

    private Set<Phase> getAutoPassPhases(HttpMessage request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("autoPassPhases".equals(cookie.name())) {
                    final String[] phases = cookie.value().split("0");
                    Set<Phase> result = new HashSet<>();
                    for (String phase : phases)
                        result.add(Phase.valueOf(phase));
                    return result;
                }
            }
            for (Cookie cookie : cookies) {
                if ("autoPass".equals(cookie.name()) && "false".equals(cookie.value()))
                    return Collections.emptySet();
            }
        }
        return _autoPassDefault;
    }

    private static final class SerializationVisitor implements ParticipantCommunicationVisitor {
        private final Document _doc;
        private final Element _element;

        private SerializationVisitor(Document doc, Element element) {
            _doc = doc;
            _element = element;
        }

        @Override
        public final void visitChannelNumber(int channelNumber) {
            _element.setAttribute("cn", String.valueOf(channelNumber));
        }

        @Override
        public final void visitGameEvents(GameCommunicationChannel channel) {
            channel.serializeConsumedEvents(_doc, _element);
        }

        @Override
        public final void visitClock(Map<String, Integer> secondsLeft) {
            _element.appendChild(serializeClocks(_doc, secondsLeft));
        }

        private static Node serializeClocks(Document doc, Map<String, Integer> secondsLeft) {
            Element clocks = doc.createElement("clocks");
            for (Map.Entry<String, Integer> userClock : secondsLeft.entrySet()) {
                Element clock = doc.createElement("clock");
                clock.setAttribute("participantId", userClock.getKey());
                String clockString = String.valueOf(userClock.getValue());
                clock.appendChild(doc.createTextNode(clockString));
                clocks.appendChild(clock);
            }

            return clocks;
        }

        @Override
        public void process(int channelNumber, GameCommunicationChannel communicationChannel,
                            Map<String, Integer> secondsLeft) {
            visitChannelNumber(channelNumber);
            visitGameEvents(communicationChannel);
            visitClock(secondsLeft);
        }

    }
}