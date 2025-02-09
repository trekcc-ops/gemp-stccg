package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;


public class LeaveQueueRequestHandler implements UriRequestHandlerNew {
    private final String _queueId;
    LeaveQueueRequestHandler(
        @JsonProperty("queueId")
        String queueId
    ) {
        _queueId = queueId;
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = getResourceOwnerSafely(request, serverObjects);
        HallServer hallServer = serverObjects.getHallServer();
        hallServer.leaveQueue(_queueId, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}