package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.HallServer;


public class LeaveTournamentRequestHandler implements UriRequestHandler {
    private final String _tournamentId;
    private final HallServer _hallServer;
    LeaveTournamentRequestHandler(
        @JsonProperty("tournamentId")
        String tournamentId,
        @JacksonInject HallServer hallServer
    ) {
        _tournamentId = tournamentId;
        _hallServer = hallServer;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        _hallServer.dropFromTournament(_tournamentId, resourceOwner);
        responseWriter.writeXmlOkResponse();
    }

}