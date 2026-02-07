package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.hall.HallServer;

public class GetDailyMessageRequestHandler implements UriRequestHandler {

    private final HallServer _hallServer;

    GetDailyMessageRequestHandler(@JacksonInject HallServer hallServer) {
        _hallServer = hallServer;
    }

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        String dailyMessage = _hallServer.getDailyMessage();
        if(dailyMessage != null) {
            responseWriter.writeJsonResponse(HTMLUtils.replaceNewlines(dailyMessage));
        }
    }
}