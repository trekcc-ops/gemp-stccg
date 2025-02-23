package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.DateUtils;
import com.gempukku.stccg.async.*;
import com.gempukku.stccg.async.handler.*;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallCommunicationChannel;
import com.gempukku.stccg.hall.HallServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


public class UpdateHallRequestHandler implements UriRequestHandler {
    private final static int SIGNUP_REWARD = 20000;
    private final static int WEEKLY_REWARD = 5000;
    private static final Logger LOGGER = LogManager.getLogger(UpdateHallRequestHandler.class);
    private final int _channelNumber;

    UpdateHallRequestHandler(
            @JsonProperty("channelNumber")
            int channelNumber
    ) {
        _channelNumber = channelNumber;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        PlayerDAO playerDAO = serverObjects.getPlayerDAO();
        CollectionsManager collectionsManager = serverObjects.getCollectionsManager();

        processLoginReward(resourceOwner, playerDAO, collectionsManager);

        HallServer hallServer = serverObjects.getHallServer();
        LongPollingSystem pollingSystem = serverObjects.getLongPollingSystem();

        try {
            HallCommunicationChannel commChannel = hallServer.getCommunicationChannel(resourceOwner, _channelNumber);
            LongPollingResource polledResource = new HallUpdateLongPollingResource(
                    commChannel, request, resourceOwner, responseWriter, hallServer, collectionsManager);
            pollingSystem.processLongPollingResource(polledResource, commChannel);
        }
        catch (HttpProcessingException exp) {
            logHttpError(LOGGER, exp.getStatus(), request.uri(), exp);
            responseWriter.writeError(exp.getStatus());
        }
    }

    final void processLoginReward(User user, PlayerDAO playerDAO, CollectionsManager collectionsManager)
            throws Exception {
        String userName = user.getName();
        synchronized (userName.intern()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
            int latestMonday = DateUtils.getMondayBeforeOrOn(now);

            Integer lastReward = user.getLastLoginReward();
            if (lastReward == null) {
                playerDAO.setLastReward(user, latestMonday);
                collectionsManager.addCurrencyToPlayerCollection(true, "Signup reward", user,
                        CollectionType.MY_CARDS, SIGNUP_REWARD);
            } else {
                if (latestMonday != lastReward) {
                    if (playerDAO.updateLastReward(user, lastReward, latestMonday))
                        collectionsManager.addCurrencyToPlayerCollection(true, "Weekly reward",
                                user, CollectionType.MY_CARDS, WEEKLY_REWARD);
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
        private final CollectionsManager _collectionsManager;

        private HallUpdateLongPollingResource(HallCommunicationChannel commChannel, GempHttpRequest request,
                                              User resourceOwner, ResponseWriter responseWriter,
                                              HallServer hallServer, CollectionsManager collectionsManager) {
            _hallCommunicationChannel = commChannel;
            _request = request;
            _resourceOwner = resourceOwner;
            _responseWriter = responseWriter;
            _hallServer = hallServer;
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
                            _hallServer, _resourceOwner, itemsToSerialize);

                    CardCollection playerCollection =
                            _collectionsManager.getPlayerCollection(_resourceOwner, CollectionType.MY_CARDS.getCode());
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
    }

}