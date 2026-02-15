package com.gempukku.stccg.async.handler.game;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LongPollingResource;
import com.gempukku.stccg.async.LongPollingSystem;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameCommunicationChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class GameUpdateLongPollingResource implements LongPollingResource {

    private final GameCommunicationChannel _gameCommunicationChannel;
    private final CardGameMediator _gameMediator;
    private final ResponseWriter _responseWriter;
    private boolean _processed;
    private static final Logger LOGGER = LogManager.getLogger(GameUpdateLongPollingResource.class);

    public GameUpdateLongPollingResource(CardGameMediator gameMediator,
                                         User user, ResponseWriter responseWriter,
                                         int channelNumber)
            throws HttpProcessingException {
        _gameCommunicationChannel = gameMediator.getCommunicationChannel(user, channelNumber);
        _gameMediator = gameMediator;
        _responseWriter = responseWriter;
    }


    public GameUpdateLongPollingResource(GameCommunicationChannel commChannel, CardGameMediator gameMediator,
                                         ResponseWriter responseWriter) {
        _gameCommunicationChannel = commChannel;
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
                String jsonString = _gameMediator.serializeEventsToString(_gameCommunicationChannel);
                _responseWriter.writeJsonResponse(jsonString);
            } catch (Exception e) {
                logHttpError(HttpURLConnection.HTTP_INTERNAL_ERROR, "game update poller", e);
                _responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
            }
            _processed = true;
        }
    }

    private static void logHttpError(int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code < 500 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            LOGGER.debug("HTTP {} response for {}", code, uri);

            // record an HTTP 400 or 500 error
        else if((code < 600))
            LOGGER.error("HTTP code {} response for {}", code, uri, exp);
    }

    public void processInSystem(LongPollingSystem longPollingSystem) {
        longPollingSystem.processLongPollingResource(this, _gameCommunicationChannel);
    }

}