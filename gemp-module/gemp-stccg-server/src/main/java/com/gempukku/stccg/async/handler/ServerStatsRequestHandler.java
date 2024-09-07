package com.gempukku.stccg.async.handler;

import com.alibaba.fastjson.JSON;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.game.GameHistoryService;
import com.gempukku.stccg.game.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TimeZone;

public class ServerStatsRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final GameHistoryService _gameHistoryService;

    private static final Logger LOGGER = LogManager.getLogger(ServerStatsRequestHandler.class);

    public ServerStatsRequestHandler(Map<Type, Object> context) {
        super(context);

        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
            String participantId = getQueryParameterSafely(queryDecoder, "participantId");
            String startDay = getQueryParameterSafely(queryDecoder, "startDay");
            String length = getQueryParameterSafely(queryDecoder, "length");

            User resourceOwner = getResourceOwnerSafely(request, participantId);

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
                    default -> throw new HttpProcessingException(400);
                }

                var stats = new JSONDefs.PlayHistoryStats();
                stats.ActivePlayers = _gameHistoryService.getActivePlayersCount(from, to);
                stats.GamesCount = _gameHistoryService.getGamesPlayedCount(from, to);
                stats.StartDate = from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.EndDate = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                stats.Stats = _gameHistoryService.getGameHistoryStatistics(from, to);

                responseWriter.writeJsonResponse(JSON.toJSONString(stats));
            } catch (Exception exp) {
                logHttpError(LOGGER, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }
}
