package com.gempukku.stccg.async.handler.admin;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.hall.HallServer;

public class GetDailyMessageRequestHandler implements UriRequestHandler, AdminRequestHandler {

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        HallServer hallServer = serverObjects.getHallServer();
        String dailyMessage = hallServer.getDailyMessage();
        if(dailyMessage != null) {
            responseWriter.writeJsonResponse(HTMLUtils.replaceNewlines(dailyMessage));
        }
    }
}