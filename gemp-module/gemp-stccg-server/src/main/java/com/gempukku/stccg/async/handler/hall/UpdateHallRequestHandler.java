package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.HallCommunicationChannel;
import com.gempukku.stccg.hall.HallServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class UpdateHallRequestHandler implements UriRequestHandler {
    private final static int SIGNUP_REWARD = 20000;
    private final static int WEEKLY_REWARD = 5000;
    private static final Logger LOGGER = LogManager.getLogger(UpdateHallRequestHandler.class);
    private final int _channelNumber;
    private final CollectionsManager _collectionsManager;
    private final HallServer _hallServer;
    private final LongPollingSystem _longPollingSystem;
    private final GameServer _gameServer;

    UpdateHallRequestHandler(
            @JsonProperty("channelNumber")
            int channelNumber,
            @JacksonInject CollectionsManager collectionsManager,
            @JacksonInject HallServer hallServer,
            @JacksonInject LongPollingSystem longPollingSystem,
            @JacksonInject GameServer gameServer) {
        _channelNumber = channelNumber;
        _collectionsManager = collectionsManager;
        _hallServer = hallServer;
        _gameServer = gameServer;
        _longPollingSystem = longPollingSystem;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        processLoginReward(resourceOwner);
        try {
            HallCommunicationChannel commChannel = _hallServer.getCommunicationChannel(resourceOwner, _channelNumber);
            LongPollingResource polledResource = new HallUpdateLongPollingResource(
                    commChannel, request, resourceOwner, responseWriter, _hallServer, _collectionsManager,
                    _gameServer);
            polledResource.processInSystem(_longPollingSystem);
        }
        catch (HttpProcessingException exp) {
            logHttpError(LOGGER, exp.getStatus(), request.uri(), exp);
            responseWriter.writeError(exp.getStatus());
        }
    }

    final void processLoginReward(User user)
            throws Exception {
        String userName = user.getName();
        synchronized (userName.intern()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
            ZonedDateTime lastMondayDate = now.minusDays(now.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
            int latestMonday = Integer.parseInt(lastMondayDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            Integer lastReward = user.getLastLoginReward();
            if (lastReward == null) {
                _collectionsManager.setLastReward(user, latestMonday);
                _collectionsManager.addCurrencyToPlayerCollection(true, "Signup reward", user,
                        SIGNUP_REWARD);
            } else {
                if (latestMonday != lastReward) {
                    if (_collectionsManager.updateLastReward(user, lastReward, latestMonday))
                        _collectionsManager.addCurrencyToPlayerCollection(true, "Weekly reward",
                                user, WEEKLY_REWARD);
                }
            }
        }
    }


    private class HallUpdateLongPollingResource implements LongPollingResource {
        private final GempHttpRequest _request;
        private final HallCommunicationChannel _hallCommunicationChannel;
        private final User _resourceOwner;
        private final ResponseWriter _responseWriter;
        private boolean _processed;
        private final HallServer _hallServer;
        private final GameServer _gameServer;
        private final CollectionsManager _collectionsManager;

        private HallUpdateLongPollingResource(HallCommunicationChannel commChannel, GempHttpRequest request,
                                              User resourceOwner, ResponseWriter responseWriter,
                                              HallServer hallServer, CollectionsManager collectionsManager,
                                              GameServer gameServer) {
            _hallCommunicationChannel = commChannel;
            _request = request;
            _resourceOwner = resourceOwner;
            _responseWriter = responseWriter;
            _hallServer = hallServer;
            _gameServer = gameServer;
            _collectionsManager = collectionsManager;
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
                            _hallServer, _gameServer, _resourceOwner, itemsToSerialize);

                    CardCollection playerCollection =
                            _collectionsManager.getPlayerMyCardsCollection(_resourceOwner);
                    itemsToSerialize.put("currency", playerCollection.getCurrency());

                    String jsonString = new ObjectMapper().writeValueAsString(itemsToSerialize);
                    _responseWriter.writeJsonResponse(jsonString);
                } catch (Exception exp) {
                    logHttpError(LOGGER, HttpURLConnection.HTTP_INTERNAL_ERROR, _request.uri(), exp);
                    _responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
                }
                _processed = true;
            }
        }

        @Override
        public void processInSystem(LongPollingSystem system) {
            system.processLongPollingResource(this, _hallCommunicationChannel);
        }
    }

}