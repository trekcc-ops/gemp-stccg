package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

public class LoginRequestHandler implements UriRequestHandler {

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
    }

    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {

        Map<String, String> parameters = getParameters(request);
        String login = parameters.get("login");
        String password = parameters.get("password");

        if (login == null || password == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        User player = serverObjects.getPlayerDAO().loginUser(login, password);
        if (player != null) {
            player.checkLogin();
            Map<String, String> userReturningHeaders = logUserReturningHeaders(remoteIp, login, serverObjects);
            responseWriter.writeEmptyXmlResponseWithHeaders(userReturningHeaders);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
    }

    Map<String, String> getParameters(HttpRequest request) throws IOException {
        Map<String, String> result = new HashMap<>();
        InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                if (data instanceof Attribute attribute) {
                    result.put(attribute.getName(), attribute.getValue());
                }
            }
        } finally {
            postDecoder.destroy();
        }
        return result;
    }

    final Map<String, String> logUserReturningHeaders(String remoteIp, String login, ServerObjects objects) throws SQLException {
        objects.getPlayerDAO().updateLastLoginIp(login, remoteIp);

        String sessionId = objects.getLoggedUserHolder().logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }




}