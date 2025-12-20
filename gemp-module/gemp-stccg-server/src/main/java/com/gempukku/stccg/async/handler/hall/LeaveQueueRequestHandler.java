package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;


public class LeaveQueueRequestHandler implements UriRequestHandler {
    private final String _queueId;
    private final HallServer _hallServer;
    LeaveQueueRequestHandler(
        @JsonProperty("queueId")
        String queueId,
        @JacksonInject HallServer hallServer
    ) {
        _queueId = queueId;
        _hallServer = hallServer;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        _hallServer.leaveQueue(_queueId, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}