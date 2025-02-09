package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallException;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JoinQueueRequestHandler implements UriRequestHandlerNew {
    private static final Logger LOGGER = LogManager.getLogger(JoinQueueRequestHandler.class);

    private final String _queueId;
    private final String _deckName;
    JoinQueueRequestHandler(
        @JsonProperty("queueId")
        String queueId,
        @JsonProperty("deckName")
        String deckName
    ) {
        _queueId = queueId;
        _deckName = deckName;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        HallServer hallServer = serverObjects.getHallServer();
        try {
            hallServer.joinQueue(_queueId, resourceOwner, _deckName);
            responseWriter.writeXmlOkResponse();
        } catch (HallException e) {
            if(doNotIgnoreError(e)) {
                LOGGER.error("Error response for {}", request.uri(), e);
            }
            responseWriter.writeXmlMarshalExceptionResponse(e.getMessage());
        }
    }

    private static boolean doNotIgnoreError(Exception ex) {
        String msg = ex.getMessage();

        if ((msg != null && msg.contains("You don't have a deck registered yet"))) return false;
        assert msg != null;
        return !msg.contains("Your selected deck is not valid for this format");
    }

}