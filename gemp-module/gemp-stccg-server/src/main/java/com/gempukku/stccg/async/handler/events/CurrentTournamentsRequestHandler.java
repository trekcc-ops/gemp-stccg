package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentService;

import java.util.List;

@JsonIgnoreProperties("participantId")
public class CurrentTournamentsRequestHandler implements UriRequestHandler {

    private final List<Tournament> _liveTournaments;

    CurrentTournamentsRequestHandler(@JacksonInject TournamentService tournamentService) {
        _liveTournaments = tournamentService.getLiveTournaments();
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        String jsonString = new ObjectMapper().writeValueAsString(_liveTournaments);
        responseWriter.writeJsonResponse(jsonString);
    }


}