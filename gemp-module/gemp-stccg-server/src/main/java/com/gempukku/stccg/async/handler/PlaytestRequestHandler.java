package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.db.DBData;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.List;

public class PlaytestRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    private final PlayerDAO _playerDAO;

    public PlaytestRequestHandler(ServerObjects objects) {
        super(objects);
        _playerDAO = objects.getPlayerDAO();
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if ("/addTesterFlag".equals(uri) && request.method() == HttpMethod.POST) {
            addTesterFlag(request, responseWriter);
        } else if ("/removeTesterFlag".equals(uri) && request.method() == HttpMethod.POST) {
            removeTesterFlag(request, responseWriter);
        } else if ("/getTesterFlag".equals(uri) && request.method() == HttpMethod.GET) {
            getTesterFlag(request, responseWriter);
        } else if ("/getRecentReplays".equals(uri) && request.method() == HttpMethod.POST) {
            getRecentReplays(request, responseWriter);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

    private void addTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            User player = getResourceOwnerSafely(request, null);

            _playerDAO.addPlayerFlag(player.getName(), User.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void removeTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            User player = getResourceOwnerSafely(request, null);

            _playerDAO.removePlayerFlag(player.getName(), User.Type.PLAY_TESTER);

            responseWriter.writeHtmlResponse("OK");

        } finally {
            postDecoder.destroy();
        }
    }

    private void getTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
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
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {

            String format = getFormParameterSafely(postDecoder, "format");
            int count = Integer.parseInt(getFormParameterSafely(postDecoder, "count"));

            final List<DBData.GameHistory> gameHistory = _gameHistoryService.getGameHistoryForFormat(format, count);

            responseWriter.writeJsonResponse(JsonUtils.toJsonString(gameHistory));

        } finally {
            postDecoder.destroy();
        }
    }

}