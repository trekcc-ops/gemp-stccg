package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.CacheManager;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.tournament.TournamentService;

import java.util.HashMap;
import java.util.Map;

public class ClearCacheRequestHandler implements UriRequestHandler, AdminRequestHandler {

    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final CacheManager _cacheManager;

    ClearCacheRequestHandler(@JacksonInject LeagueService leagueService,
                             @JacksonInject TournamentService tournamentService,
                             @JacksonInject CacheManager cacheManager
    ) {
        _leagueService = leagueService;
        _tournamentService = tournamentService;
        _cacheManager = cacheManager;
    }
    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        validateAdmin(request);
        _leagueService.clearCache();
        _tournamentService.clearCache();

        int before = _cacheManager.getTotalCount();
        _cacheManager.clearCaches();
        int after = _cacheManager.getTotalCount();

        Map<String, Integer> result = new HashMap<>();
        result.put("before", before);
        result.put("after", after);
        responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(result));
    }
}