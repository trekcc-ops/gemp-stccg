package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.hall.HallCommunicationChannel;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetHallRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(GetHallRequestHandler.class);

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter, ServerObjects serverObjects)
            throws Exception {
        try {
            User user = request.user();
            Map<Object, Object> hallMap = new HashMap<>();
            HallServer _hallServer = serverObjects.getHallServer();

            CardCollection playerCollection =
                    serverObjects.getCollectionsManager().getPlayerCollection(user, CollectionType.MY_CARDS.getCode());
            hallMap.put("currency", playerCollection.getCurrency());

            HallCommunicationChannel channel = _hallServer.signupUserForHallAndGetChannel(user);
            channel.processCommunicationChannel(_hallServer, user, hallMap);

            hallMap.put("formats", getFormats(user, serverObjects));
            String jsonString = new ObjectMapper().writeValueAsString(hallMap);
            responseWriter.writeJsonResponse(jsonString);
        } catch (HttpProcessingException exp) {
            int expStatus = exp.getStatus();
            logHttpError(LOGGER, expStatus, request.uri(), exp);
            responseWriter.writeError(expStatus);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", request.uri(), exp);
            responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }

    private List<Map<?, ?>> getFormats(User player, ServerObjects serverObjects) {
        List<Map<?, ?>> formats = new ArrayList<>();
        LeagueService _leagueService = serverObjects.getLeagueService();
        for (Map.Entry<String, GameFormat> format : serverObjects.getFormatLibrary().getHallFormats().entrySet()) {

            //playtest formats are opt-in
            if (!format.getKey().startsWith("test") || player.getType().contains("p")) {

                Map<String, String> formatsMap = new HashMap<>();
                formatsMap.put("type", format.getKey());
                formatsMap.put("name", format.getValue().getName());
                formats.add(formatsMap);
            }
        }
        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueSeriesData seriesData = _leagueService.getCurrentLeagueSeries(league);
            if (seriesData != null && _leagueService.isPlayerInLeague(league, player)) {
                Map<String, String> formatMap = new HashMap<>();
                formatMap.put("type", league.getType());
                formatMap.put("name", league.getName());
                formats.add(formatMap);
            }
        }
        return formats;
    }

}