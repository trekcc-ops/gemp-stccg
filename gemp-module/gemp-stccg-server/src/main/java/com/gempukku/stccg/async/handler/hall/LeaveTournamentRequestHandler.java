package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;


public class LeaveTournamentRequestHandler implements UriRequestHandler {
    private final String _tournamentId;
    LeaveTournamentRequestHandler(
        @JsonProperty("tournamentId")
        String tournamentId
    ) {
        _tournamentId = tournamentId;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        HallServer _hallServer = serverObjects.getHallServer();
        _hallServer.dropFromTournament(_tournamentId, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}