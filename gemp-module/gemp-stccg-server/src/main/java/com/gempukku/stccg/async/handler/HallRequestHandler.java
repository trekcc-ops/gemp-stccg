package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.HallCommunicationChannel;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HallRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(HallRequestHandler.class);
    private final HallServer _hallServer;
    private final LeagueService _leagueService;
    private final LongPollingSystem _longPollingSystem;

    HallRequestHandler(ServerObjects objects) {
        super(objects);
        _hallServer = objects.getHallServer();
        _leagueService = objects.getLeagueService();
        _longPollingSystem = objects.getLongPollingSystem();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            getHall(request, responseWriter);
        } else if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            createTable(request, responseWriter);
        } else if ("/update".equals(uri) && request.method() == HttpMethod.POST) {
            updateHall(request, responseWriter);
        } else if ("/errata/json".equals(uri) && request.method() == HttpMethod.GET) {
            getErrataInfo(responseWriter);
        } else if (uri.startsWith("/format/") && request.method() == HttpMethod.GET) {
            int beginIndex = "/format/".length();
            getFormat(uri.substring(beginIndex), responseWriter);
        } else if (uri.startsWith("/queue/") && request.method() == HttpMethod.POST) {
            int beginIndex = "/queue/".length();
            if (uri.endsWith("/leave")) {
                int endIndex = uri.length() - "/leave".length();
                leaveQueue(request, uri.substring(beginIndex, endIndex), responseWriter);
            } else {
                joinQueue(request, uri.substring(beginIndex), responseWriter);
            }
        } else if (uri.startsWith("/tournament/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            int beginIndex = "/tournament/".length();
            int endIndex = uri.length() - "/leave".length();
            dropFromTournament(request, uri.substring(beginIndex, endIndex), responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/leave") && request.method() == HttpMethod.POST) {
            int endIndex = uri.length() - "/leave".length();
            leaveTable(request, uri.substring(1, endIndex), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.POST) {
            joinTable(request, uri.substring(1), responseWriter);
        } else {
            responseWriter.writeError(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void joinTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder decoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(decoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);

            String deckName = getFormParameterSafely(decoder, FormParameter.deckName);
            LOGGER.debug("HallRequestHandler - calling joinTableAsPlayer function from JoinTable");

            try {
                _hallServer.joinTableAsPlayer(tableId, resourceOwner, deckName);
                responseWriter.writeXmlOkResponse();
            } catch (HallException e) {
                try {
                    //Try again assuming it's a new player using the default deck library decks
                    User libraryOwner = _playerDao.getPlayer("Librarian");
                    _hallServer.joinTableAsPlayerWithSpoofedDeck(tableId, resourceOwner, libraryOwner, deckName);
                    responseWriter.writeXmlOkResponse();
                    return;
                } catch (HallException ex) {
                    if(doNotIgnoreError(ex)) {
                        LOGGER.error("Error response for {}", request.uri(), ex);
                    }
                }
                catch (Exception ex) {
                    LOGGER.error("Additional error response for {}", request.uri(), ex);
                    throw ex;
                }
                responseWriter.writeXmlMarshalExceptionResponse(e.getMessage());
            }
        }
    }

    private void leaveTable(HttpRequest request, String tableId, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            _hallServer.leaveAwaitingTable(resourceOwner, tableId);
            responseWriter.writeXmlOkResponse();
        }
    }

    private void createTable(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            String format = getFormParameterSafely(postDecoder, FormParameter.format);
            String deckName = getFormParameterSafely(postDecoder, FormParameter.deckName);
            String timer = getFormParameterSafely(postDecoder, FormParameter.timer);
            String desc = getFormParameterSafely(postDecoder, FormParameter.desc).trim();
            String isPrivateVal = getFormParameterSafely(postDecoder, FormParameter.isPrivate);
            boolean isPrivate = (Boolean.parseBoolean(isPrivateVal));
            String isInviteOnlyVal = getFormParameterSafely(postDecoder, FormParameter.isInviteOnly);
            boolean isInviteOnly = (Boolean.parseBoolean(isInviteOnlyVal));
            //To prevent annoyance, super long glacial games are hidden from everyone except
            // the participants and admins.
            boolean isVisible = !timer.toLowerCase().equals(GameTimer.GLACIAL_TIMER.name());

            User resourceOwner = getResourceOwnerSafely(request, participantId);

            if(isInviteOnly) {
                String errorMessage = "";
                if(desc.isEmpty()) {
                    errorMessage = "Invite-only games must have your intended opponent in the description";
                } else if(desc.equalsIgnoreCase(resourceOwner.getName())) {
                    errorMessage = "Absolutely no playing with yourself!!  Private matches must be with someone else.";
                } else {
                    try {
                        var player = _playerDao.getPlayer(desc);
                        if(player == null)
                            throw new RuntimeException();
                    } catch(RuntimeException ex) {
                        errorMessage = "Cannot find player '" + desc + "'. " +
                                "Check your spelling and capitalization and ensure it is exact.";
                    }
                }
                if (!errorMessage.isEmpty()) {
                    responseWriter.writeXmlMarshalExceptionResponse(errorMessage);
                    return;
                }
            }



            try {
                _hallServer.createNewTable(
                        format, resourceOwner, deckName, timer, desc, isInviteOnly, isPrivate, !isVisible);
                responseWriter.writeXmlOkResponse();
            }
            catch (HallException e) {
                try
                {
                    //try again assuming it's a new player with one of the default library decks selected
                    _hallServer.createNewTable(format, resourceOwner, _playerDao.getPlayer("Librarian"),
                            deckName, timer, "(New Player) " + desc, isInviteOnly, isPrivate, !isVisible);
                    responseWriter.writeXmlOkResponse();
                    return;
                }
                catch (HallException ignored) { }
                responseWriter.writeXmlMarshalExceptionResponse(e);
            }
        }
        catch (Exception ex)
        {
            //This is a worthless error that doesn't need to be spammed into the log
            if(doNotIgnoreError(ex)) {
                LOGGER.error("Error response for {}", request.uri(), ex);
            }
            responseWriter.writeXmlMarshalExceptionResponse(
                    "Failed to create table. Please try again later.");
        }
    }



    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

    private void dropFromTournament(HttpRequest request, String tournamentId, ResponseWriter responseWriter)
            throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            _hallServer.dropFromTournament(tournamentId, resourceOwner);
            responseWriter.writeXmlOkResponse();
        }
    }

    private void joinQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder decoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(decoder, FormParameter.participantId);
            String deckName = getFormParameterSafely(decoder, FormParameter.deckName);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            try {
                _hallServer.joinQueue(queueId, resourceOwner, deckName);
                responseWriter.writeXmlOkResponse();
            } catch (HallException e) {
                if(doNotIgnoreError(e)) {
                    LOGGER.error("Error response for {}", request.uri(), e);
                }
                responseWriter.writeXmlMarshalExceptionResponse(e.getMessage());
            }
        }
    }

    private void leaveQueue(HttpRequest request, String queueId, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder decoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(decoder, FormParameter.participantId);
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            _hallServer.leaveQueue(queueId, resourceOwner);
            responseWriter.writeXmlOkResponse();
        }
    }

    private void getFormat(String format, ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        GameFormat gameFormat = _serverObjects.getFormatLibrary().get(format);
        result.append(HTMLUtils.serializeFormatForHall(gameFormat, _serverObjects.getCardBlueprintLibrary()));
        responseWriter.writeHtmlResponse(result.toString());
    }

    private void getFormats(ResponseWriter responseWriter) throws CardNotFoundException {
        StringBuilder result = new StringBuilder();
        FormatLibrary formatLibrary = _serverObjects.getFormatLibrary();
        for (GameFormat gameFormat : formatLibrary.getHallFormats().values())
            result.append(HTMLUtils.serializeFormatForHall(gameFormat, _serverObjects.getCardBlueprintLibrary()));
        responseWriter.writeHtmlResponse(result.toString());
    }

    private void getErrataInfo(ResponseWriter responseWriter) throws JsonProcessingException {
        String jsonString = _jsonMapper.writeValueAsString(_cardBlueprintLibrary.getErrata());
        responseWriter.writeJsonResponse(jsonString);
    }

    private void getHall(HttpRequest request, ResponseWriter responseWriter) {
        try {
            User resourceOwner = getUserIdFromCookiesOrUri(request);
            User player = getResourceOwnerSafely(request, null);

            Map<Object, Object> hallMap = new HashMap<>();

            CardCollection playerCollection =
                    _collectionsManager.getPlayerCollection(resourceOwner, CollectionType.MY_CARDS.getCode());
            hallMap.put("currency", playerCollection.getCurrency());

            HallCommunicationChannel channel = _hallServer.signupUserForHallAndGetChannel(resourceOwner);
            channel.processCommunicationChannel(_hallServer, player, hallMap);

            hallMap.put("formats", getFormats(player));
            String jsonString = _jsonMapper.writeValueAsString(hallMap);
            responseWriter.writeJsonResponse(jsonString);
        } catch (HttpProcessingException exp) {
            int expStatus = exp.getStatus();
            logHttpError(LOGGER, expStatus, request.uri(), exp);
            responseWriter.writeError(expStatus);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }

    private List<Map<?, ?>> getFormats(User player) {
        List<Map<?, ?>> formats = new ArrayList<>();
        for (Map.Entry<String, GameFormat> format : _serverObjects.getFormatLibrary().getHallFormats().entrySet()) {

            //playtest formats are opt-in
            if (!format.getKey().startsWith("test") || player.getType().contains("p")) {

                Map<String, String> formatsMap = new HashMap<>();
                formatsMap.put("type", format.getKey());
                formatsMap.put("name", format.getValue().getName());
                formats.add(formatsMap);
            }
        }
        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueSeriesData seriesData = _leagueService.getCurrentLeagueSeries(league);
            if (seriesData != null && _leagueService.isPlayerInLeague(league, player)) {
                Map<String, String> formatMap = new HashMap<>();
                formatMap.put("type", league.getType());
                formatMap.put("name", league.getName());
                formats.add(formatMap);
            }
        }
        return formats;
    }

    private void updateHall(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String participantId = getFormParameterSafely(postDecoder, FormParameter.participantId);
            int channelNumber = Integer.parseInt(getFormParameterSafely(postDecoder, FormParameter.channelNumber));
            User resourceOwner = getResourceOwnerSafely(request, participantId);
            processLoginReward(resourceOwner.getName());

            try {
                HallCommunicationChannel commChannel =
                        _hallServer.getCommunicationChannel(resourceOwner, channelNumber);
                LongPollingResource polledResource =
                        new HallUpdateLongPollingResource(commChannel, request, resourceOwner, responseWriter);
                _longPollingSystem.processLongPollingResource(polledResource, commChannel);
            }
            catch (HttpProcessingException exp) {
                logHttpError(LOGGER, exp.getStatus(), request.uri(), exp);
                responseWriter.writeError(exp.getStatus());
            }
        }
    }

    private class HallUpdateLongPollingResource implements LongPollingResource {
        private final HttpRequest _request;
        private final HallCommunicationChannel _hallCommunicationChannel;
        private final User _resourceOwner;
        private final ResponseWriter _responseWriter;
        private boolean _processed;

        private HallUpdateLongPollingResource(HallCommunicationChannel commChannel, HttpRequest request,
                                              User resourceOwner, ResponseWriter responseWriter) {
            _hallCommunicationChannel = commChannel;
            _request = request;
            _resourceOwner = resourceOwner;
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
                    Map<Object, Object> itemsToSerialize = new HashMap<>();

                    _hallCommunicationChannel.processCommunicationChannel(
                            _hallServer, _resourceOwner, itemsToSerialize);

                    CardCollection playerCollection =
                            _collectionsManager.getPlayerCollection(_resourceOwner, CollectionType.MY_CARDS.getCode());
                    itemsToSerialize.put("currency", playerCollection.getCurrency());

                    Map<String, String> headers = new HashMap<>();

                    String jsonString = _jsonMapper.writeValueAsString(itemsToSerialize);
                    _responseWriter.writeJsonResponseWithHeaders(jsonString, headers);
                } catch (Exception exp) {
                    logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, _request.uri(), exp);
                    _responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
                }
                _processed = true;
            }
        }
    }

}