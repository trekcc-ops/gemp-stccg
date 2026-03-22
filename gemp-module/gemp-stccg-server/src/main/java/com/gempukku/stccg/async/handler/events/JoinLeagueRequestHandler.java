package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueService;

import java.net.HttpURLConnection;

public class JoinLeagueRequestHandler implements UriRequestHandler {

    private final String _leagueType;
    private final LeagueService _leagueService;
    JoinLeagueRequestHandler(
            @JsonProperty("leagueType")
            String leagueType,
            @JacksonInject LeagueService leagueService
    ) {
        _leagueType = leagueType;
        _leagueService = leagueService;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        User resourceOwner = request.user();
        try {
            League league = _leagueService.getLeagueById(_leagueType);
            if (!_leagueService.playerJoinsLeague(league, resourceOwner, request.ip()))
                throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
        } catch(LeagueNotFoundException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
        responseWriter.writeXmlOkResponse();
    }

}