package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JoinQueueRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(JoinQueueRequestHandler.class);

    private final String _queueId;
    private final String _deckName;
    private final CardBlueprintLibrary _cardBlueprintLibrary;
    private final DeckDAO _deckDAO;
    private final HallServer _hallServer;

    JoinQueueRequestHandler(
            @JsonProperty("queueId")
        String queueId,
            @JsonProperty("deckName")
        String deckName,
            @JacksonInject HallServer hallServer,
            @JacksonInject CardBlueprintLibrary cardBlueprintLibrary,
            @JacksonInject DeckDAO deckDAO) {
        _queueId = queueId;
        _deckName = deckName;
        _deckDAO = deckDAO;
        _hallServer = hallServer;
        _cardBlueprintLibrary = cardBlueprintLibrary;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        if (_hallServer.isShutdown()) {
            responseWriter.writeXmlMarshalExceptionResponse("Server is in shutdown mode. Server will be restarted after all running games are finished.");
        } else {
            try {
                _hallServer.addPlayerToQueue(_queueId, resourceOwner, _deckName, _cardBlueprintLibrary, _deckDAO);
                responseWriter.writeXmlOkResponse();
            } catch (HallException e) {
                if (doNotIgnoreError(e)) {
                    LOGGER.error("Error response for {}", request.uri(), e);
                }
                responseWriter.writeXmlMarshalExceptionResponse(e.getMessage());
            }
        }
    }

    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

}