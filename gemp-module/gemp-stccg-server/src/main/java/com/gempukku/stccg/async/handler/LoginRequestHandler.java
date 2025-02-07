package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

@JsonIgnoreProperties("participantId")
public class LoginRequestHandler implements UriRequestHandlerNew {

    private final String _userId;
    private final String _password;

    public LoginRequestHandler(
            @JsonProperty(value = "login", required = true)
            String userId,
            @JsonProperty(value = "password", required = true)
            String password
    ) {
        _userId = userId;
        _password = password;
    }

    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp,
                                    ServerObjects serverObjects)
            throws Exception {

        if (_userId == null || _password == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        User player = serverObjects.getPlayerDAO().loginUser(_userId, _password);
        if (player != null) {
            player.checkLogin();
            Map<String, String> userReturningHeaders = logUserReturningHeaders(remoteIp, _userId, serverObjects);
            responseWriter.writeEmptyXmlResponseWithHeaders(userReturningHeaders);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
    }

    final Map<String, String> logUserReturningHeaders(String remoteIp, String login, ServerObjects objects)
            throws SQLException {
        objects.getPlayerDAO().updateLastLoginIp(login, remoteIp);

        String sessionId = objects.getLoggedUserHolder().logUser(login);
        return Collections.singletonMap(
                SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode("loggedUser", sessionId));
    }




}