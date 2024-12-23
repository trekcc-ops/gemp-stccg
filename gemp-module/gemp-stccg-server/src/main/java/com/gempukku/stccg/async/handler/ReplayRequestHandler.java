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

public class ReplayRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {
    private final GameRecorder _gameRecorder;
    private final static int BYTE_LENGTH = 1024;
    private static final Logger LOGGER = LogManager.getLogger(ReplayRequestHandler.class);

    public ReplayRequestHandler(ServerObjects objects) {
        super(objects);
        _gameRecorder = objects.getGameRecorder();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            String replayId = uri.substring(1);

            if (!replayId.contains("$") || replayId.contains("."))
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            final String[] split = replayId.split("\\$");
            if (split.length != 2)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            final InputStream recordedGame = _gameRecorder.getRecordedGame(split[0], split[1]);
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
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}