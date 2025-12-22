package com.gempukku.stccg.async.handler.hall;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.hall.HallCommunicationChannel;
import com.gempukku.stccg.hall.HallServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueSeries;
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
    private final HallServer _hallServer;
    private final CollectionsManager _collectionsManager;
    private final FormatLibrary _formatLibrary;
    private final LeagueService _leagueService;
    private final GameServer _gameServer;

    GetHallRequestHandler(@JacksonInject HallServer hallServer,
                          @JacksonInject CollectionsManager collectionsManager,
                          @JacksonInject FormatLibrary formatLibrary,
                          @JacksonInject LeagueService leagueService,
                          @JacksonInject GameServer gameServer) {
        _hallServer = hallServer;
        _collectionsManager = collectionsManager;
        _formatLibrary = formatLibrary;
        _leagueService = leagueService;
        _gameServer = gameServer;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        try {
            User user = request.user();
            Map<Object, Object> hallMap = new HashMap<>();

            CardCollection playerCollection =
                    _collectionsManager.getPlayerMyCardsCollection(user);
            hallMap.put("currency", playerCollection.getCurrency());

            HallCommunicationChannel channel = _hallServer.signupUserForHallAndGetChannel(user);
            channel.processCommunicationChannel(_hallServer, _gameServer, user, hallMap);

            hallMap.put("formats", getFormats(user));
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

    private List<Map<?, ?>> getFormats(User player) {
        List<Map<?, ?>> formats = new ArrayList<>();
        for (Map.Entry<String, GameFormat> format : _formatLibrary.getHallFormats().entrySet()) {

            //playtest formats are opt-in
            if (!format.getKey().startsWith("test") || player.getType().contains("p")) {

                Map<String, String> formatsMap = new HashMap<>();
                formatsMap.put("type", format.getKey());
                formatsMap.put("name", format.getValue().getName());
                formats.add(formatsMap);
            }
        }
        for (League league : _leagueService.getActiveLeagues()) {
            final LeagueSeries seriesData = _leagueService.getCurrentLeagueSeriesNew(league);
            if (seriesData != null && _leagueService.isPlayerInLeague(league, player)) {
                Map<String, String> formatMap = new HashMap<>();
                formatMap.put("type", String.valueOf(league.getLeagueId()));
                formatMap.put("name", league.getName());
                formats.add(formatMap);
            }
        }
        return formats;
    }

}