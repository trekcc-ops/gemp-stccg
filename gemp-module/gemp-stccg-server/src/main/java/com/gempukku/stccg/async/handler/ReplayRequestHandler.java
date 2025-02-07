package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.game.GameRecorder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class ReplayRequestHandler implements UriRequestHandlerNew {
    private final static int BYTE_LENGTH = 1024;
    private static final Logger LOGGER = LogManager.getLogger(ReplayRequestHandler.class);

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {
        String replayId = uri.substring(1);

        if (!replayId.contains("$") || replayId.contains("."))
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        final String[] split = replayId.split("\\$");
        if (split.length != 2)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final InputStream recordedGame = serverObjects.getGameRecorder().getRecordedGame(split[0], split[1]);
        try (recordedGame) {
            if (recordedGame == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            byte[] bytes = new byte[BYTE_LENGTH];
            int count;
            while ((count = recordedGame.read(bytes)) != -1)
                outputStream.write(bytes, 0, count);
        } catch (IOException exp) {
            LOGGER.error("Error 404 response for {}", request.uri(), exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }

        Map<AsciiString, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE, "application/html; charset=UTF-8");

        responseWriter.writeByteResponse(outputStream.toByteArray(), headers);
    }
}