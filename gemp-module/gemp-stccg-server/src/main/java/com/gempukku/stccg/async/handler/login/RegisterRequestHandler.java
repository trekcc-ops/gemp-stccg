package com.gempukku.stccg.async.handler.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.database.LoginInvalidException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.Map;

@JsonIgnoreProperties("participantId")
public class RegisterRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(RegisterRequestHandler.class);
    private final String _userId;
    private final String _password;

    RegisterRequestHandler(
        @JsonProperty(value = "login", required = true)
        String userId,
        @JsonProperty(value = "password", required = true)
        String password
    ) {
        _userId = userId;
        _password = password;
    }

    @Override
    public final void handleRequest(GempHttpRequest request, ResponseWriter responseWriter,
                                    ServerObjects serverObjects)
            throws Exception {
        try {
            if (serverObjects.getPlayerDAO().registerUser(_userId, _password, request.ip())) {
                Map<String, String> headers = logUserReturningHeaders(request.ip(), _userId, serverObjects);
                responseWriter.writeEmptyXmlResponseWithHeaders(headers);
            } else
                throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
        } catch (LoginInvalidException exp) {
            String requestUri = request.uri();
            logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, requestUri, exp);
            throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
        }
    }
}