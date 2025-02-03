package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.JSONData;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class ServerStatsRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerStatsRequestHandler.class);

    public ServerStatsRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request,
                                    ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String startDay = getQueryParameterSafely(queryDecoder, FormParameter.startDay);
            String length = getQueryParameterSafely(queryDecoder, FormParameter.length);

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));

                //This convoluted conversion is actually necessary, for it to be flexible enough to take
                //human-level dates such as 2023-2-13 (note the lack of zero padding)
                var from = ZonedDateTime.ofInstant(format.parse(startDay).toInstant(), ZoneOffset.UTC);

                ZonedDateTime to;

                switch (length) {
                    case "month" -> to = from.plusMonths(1);
                    case "week" -> to = from.plusDays(7);
                    case "day" -> to = from.plusDays(1);
                    default -> throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
                }

                var stats = new PlayHistoryStats();
                stats.ActivePlayers = _gameHistoryService.getActivePlayersCount(from, to);
                stats.GamesCount = _gameHistoryService.getGamesPlayedCount(from, to);
                stats.StartDate = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.EndDate = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.Stats = _gameHistoryService.getGameHistoryStatistics(from, to);
                String jsonString = _jsonMapper.writeValueAsString(stats);
                responseWriter.writeJsonResponse(jsonString);
            } catch (Exception exp) {
                logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, request.uri(), exp);
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            }
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private static class PlayHistoryStats {
        public List<JSONData.FormatStats> Stats;
        public int ActivePlayers;
        public int GamesCount;
        public String StartDate;
        public String EndDate;
    }

}