package com.gempukku.stccg.async.handler.login;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.service.AdminService;

import java.net.HttpURLConnection;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class LoginRequestHandler implements UriRequestHandler {

    private final String _userId;
    private final String _password;
    private final AdminService _adminService;

    public LoginRequestHandler(
            @JsonProperty(value = "login", required = true)
            String userId,
            @JsonProperty(value = "password", required = true)
            String password,
            @JacksonInject AdminService adminService) {
        _userId = userId;
        _password = password;
        _adminService = adminService;
    }

    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {

        if (_userId == null || _password == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        User player = _adminService.loginUser(_userId, _password);
        if (player != null) {
            player.checkLogin();
            Map<String, String> userReturningHeaders =
                    logUserReturningHeaders(request.ip(), _userId, _adminService);
            responseWriter.writeEmptyXmlResponseWithHeaders(userReturningHeaders);
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        }
    }

}