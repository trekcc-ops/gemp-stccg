package com.gempukku.stccg.async.handler.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.tournament.Tournament;

import java.util.List;

@JsonIgnoreProperties("participantId")
public class TournamentHistoryRequestHandler implements UriRequestHandler {

    private final static long SEVEN_DAYS_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        long sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_IN_MILLIS;
        List<Tournament> tournaments = serverObjects.getTournamentService().getOldTournaments(sevenDaysAgo);
        String jsonString = new ObjectMapper().writeValueAsString(tournaments);
        responseWriter.writeJsonResponse(jsonString);
    }


}