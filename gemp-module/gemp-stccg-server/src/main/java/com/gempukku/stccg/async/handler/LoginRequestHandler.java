package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.db.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;

import java.net.HttpURLConnection;
import java.util.Map;

public class LoginRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public LoginRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
            String login = getFormParameterSafely(postDecoder, "login");
            String password = getFormParameterSafely(postDecoder, "password");

            User player = _playerDao.loginUser(login, password);
            if (player != null) {
                player.checkLogin();
                Map<String, String> userReturningHeaders = logUserReturningHeaders(remoteIp, login);
                responseWriter.writeXmlResponse(null, userReturningHeaders);
            } else {
                throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

}