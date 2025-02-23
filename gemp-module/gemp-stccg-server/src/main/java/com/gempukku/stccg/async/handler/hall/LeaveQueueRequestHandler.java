package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;


public class LeaveQueueRequestHandler implements UriRequestHandler {
    private final String _queueId;
    LeaveQueueRequestHandler(
        @JsonProperty("queueId")
        String queueId
    ) {
        _queueId = queueId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        HallServer hallServer = serverObjects.getHallServer();
        hallServer.leaveQueue(_queueId, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}