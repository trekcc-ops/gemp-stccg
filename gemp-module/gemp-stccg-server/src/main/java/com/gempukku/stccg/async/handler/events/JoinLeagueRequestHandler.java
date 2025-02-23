package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueService;

import java.net.HttpURLConnection;

public class JoinLeagueRequestHandler implements UriRequestHandler {

    private final String _leagueType;
    JoinLeagueRequestHandler(
            @JsonProperty("leagueType")
            String leagueType
    ) {
        _leagueType = leagueType;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        User resourceOwner = request.user();
        LeagueService leagueService = serverObjects.getLeagueService();
        try {
            League league = leagueService.getLeagueByType(_leagueType);
            if (!leagueService.playerJoinsLeague(league, resourceOwner, request.ip()))
                throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
        } catch(LeagueNotFoundException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
        responseWriter.writeXmlOkResponse();
    }

}