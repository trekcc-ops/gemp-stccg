package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.DBDefs;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.game.GameHistoryService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PlaytestRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    private final PlayerDAO _playerDAO;
    private final GameHistoryService _gameHistoryService;

    public PlaytestRequestHandler(Map<Type, Object> context) {
        super(context);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _gameHistoryService = extractObject(context, GameHistoryService.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/addTesterFlag") && request.method() == HttpMethod.POST) {
            addTesterFlag(request, responseWriter);
        } else if (uri.equals("/removeTesterFlag") && request.method() == HttpMethod.POST) {
            removeTesterFlag(request, responseWriter);
        } else if (uri.equals("/getTesterFlag") && request.method() == HttpMethod.GET) {
            getTesterFlag(request, responseWriter);
        } else if (uri.equals("/getRecentReplays") && request.method() == HttpMethod.POST) {
            getRecentReplays(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void addTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            User player = getResourceOwnerSafely(request, null);

            _playerDAO.addPlayerFlag(player.getName(), User.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void removeTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            User player = getResourceOwnerSafely(request, null);

            _playerDAO.removePlayerFlag(player.getName(), User.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void getTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            User player = getResourceOwnerSafely(request, null);

            Document doc = createNewDoc();
            Element hasTester = doc.createElement("hasTester");

            hasTester.setAttribute("result", String.valueOf(player.hasType(User.Type.PLAY_TESTER)));

            responseWriter.writeXmlResponse(doc);

        } finally {
            postDecoder.destroy();
        }
    }

    private void getRecentReplays(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {

            String format = getFormParameterSafely(postDecoder, "format");
            int count = Integer.parseInt(getFormParameterSafely(postDecoder, "count"));

            final List<DBDefs.GameHistory> gameHistory = _gameHistoryService.getGameHistoryForFormat(format, count);

            responseWriter.writeJsonResponse(JsonUtils.toJsonString(gameHistory));

        } finally {
            postDecoder.destroy();
        }
    }

}
