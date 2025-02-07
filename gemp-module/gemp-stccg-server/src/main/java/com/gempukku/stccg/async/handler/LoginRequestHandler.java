package com.gempukku.stccg.async.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpRequest;

import java.net.HttpURLConnection;
import java.util.Map;

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

}