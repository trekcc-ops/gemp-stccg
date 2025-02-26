package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentService;

import java.util.List;

@JsonIgnoreProperties("participantId")
public class CurrentTournamentsRequestHandler implements UriRequestHandler {

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        TournamentService _tournamentService = serverObjects.getTournamentService();
        List<Tournament> liveTournaments = _tournamentService.getLiveTournaments();
        String jsonString = new ObjectMapper().writeValueAsString(liveTournaments);
        responseWriter.writeJsonResponse(jsonString);
    }


}