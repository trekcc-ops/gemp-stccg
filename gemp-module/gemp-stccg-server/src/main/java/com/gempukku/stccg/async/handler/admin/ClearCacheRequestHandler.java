package com.gempukku.stccg.async.handler.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.CacheManager;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandlerNew;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.league.LeagueService;

import java.util.HashMap;
import java.util.Map;

public class ClearCacheRequestHandler extends AdminRequestHandlerNew implements UriRequestHandlerNew {

    @Override
    public void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        validateAdmin(request);
        serverObjects.getLeagueService().clearCache();
        serverObjects.getTournamentService().clearCache();

        CacheManager cacheManager = serverObjects.getCacheManager();

        int before = cacheManager.getTotalCount();
        cacheManager.clearCaches();
        int after = cacheManager.getTotalCount();

        Map<String, Integer> result = new HashMap<>();
        result.put("before", before);
        result.put("after", after);
        responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(result));
    }
}