package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;
import io.netty.handler.codec.http.HttpRequest;


public class LeaveTableRequestHandler implements UriRequestHandlerNew {
    private final String _tableId;
    LeaveTableRequestHandler(
        @JsonProperty("tableId")
        String tableId
    ) {
        _tableId = tableId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        HallServer _hallServer = serverObjects.getHallServer();
        _hallServer.leaveAwaitingTable(resourceOwner, _tableId);
        responseWriter.writeXmlOkResponse();
    }

}