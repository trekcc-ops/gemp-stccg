package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
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
    public final void handleRequest(String uri, GempHttpRequest gempRequest, ResponseWriter responseWriter)
            throws Exception {
        HttpRequest request = gempRequest.getRequest();
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
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            User player = getResourceOwnerSafely(request);
            _playerDAO.addPlayerFlag(player.getName(), User.Type.PLAY_TESTER);
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void removeTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            User player = getResourceOwnerSafely(request);
            _playerDAO.removePlayerFlag(player.getName(), User.Type.PLAY_TESTER);
            responseWriter.writeHtmlOkResponse();
        }
    }

    private void getTesterFlag(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            User player = getResourceOwnerSafely(request);
            Document doc = createNewDoc();
            Element hasTester = doc.createElement("hasTester");
            hasTester.setAttribute("result", String.valueOf(player.hasType(User.Type.PLAY_TESTER)));
            responseWriter.writeXmlResponseWithNoHeaders(doc);
        }
    }

    private void getRecentReplays(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String format = getFormParameterSafely(postDecoder, FormParameter.format);
            int count = Integer.parseInt(getFormParameterSafely(postDecoder, FormParameter.count));
            final List<DBData.GameHistory> gameHistory = _gameHistoryService.getGameHistoryForFormat(format, count);
            String jsonString = _jsonMapper.writeValueAsString(gameHistory);
            responseWriter.writeJsonResponse(jsonString);
        }
    }

}