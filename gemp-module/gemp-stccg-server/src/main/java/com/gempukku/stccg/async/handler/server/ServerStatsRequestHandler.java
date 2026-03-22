package com.gempukku.stccg.async.handler.server;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.game.GameHistoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;

public class ServerStatsRequestHandler implements UriRequestHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerStatsRequestHandler.class);
    private final ZonedDateTime _fromDate;
    private final ZonedDateTime _toDate;
    private final GameHistoryService _gameHistoryService;
    
    ServerStatsRequestHandler(
            @JsonProperty("startDay")
            String startDayText,
            @JsonProperty("length")
            String length,
            @JacksonInject GameHistoryService gameHistoryService
    ) throws ParseException, HttpProcessingException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        //This convoluted conversion is actually necessary, for it to be flexible enough to take
        //human-level dates such as 2023-2-13 (note the lack of zero padding)
        _fromDate = ZonedDateTime.ofInstant(format.parse(startDayText).toInstant(), ZoneOffset.UTC);

        switch (length) {
            case "month" -> _toDate = _fromDate.plusMonths(1);
            case "week" -> _toDate = _fromDate.plusDays(7);
            case "day" -> _toDate = _fromDate.plusDays(1);
            default -> throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
        _gameHistoryService = gameHistoryService;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter) throws Exception {
        try {
            Map<String, Object> stats = _gameHistoryService.serializeForServerStats(_fromDate, _toDate);
            responseWriter.writeJsonResponse(new ObjectMapper().writeValueAsString(stats));
        } catch (Exception exp) {
            logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
    }

}