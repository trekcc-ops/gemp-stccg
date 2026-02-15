package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;


public class LeaveTableRequestHandler implements UriRequestHandler {
    private final String _tableId;
    private final HallServer _hallServer;
    LeaveTableRequestHandler(
        @JsonProperty("tableId")
        String tableId,
        @JacksonInject HallServer hallServer
    ) {
        _tableId = tableId;
        _hallServer = hallServer;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        _hallServer.leaveAwaitingTable(resourceOwner, _tableId);
        responseWriter.writeXmlOkResponse();
    }

}