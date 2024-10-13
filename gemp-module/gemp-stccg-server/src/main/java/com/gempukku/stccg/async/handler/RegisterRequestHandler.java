package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.db.LoginInvalidException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public class RegisterRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(RegisterRequestHandler.class);
    public RegisterRequestHandler(ServerObjects objects) {
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
            try {
                if (_playerDao.registerUser(login, password, remoteIp)) {
                    responseWriter.writeXmlResponse(null, logUserReturningHeaders(remoteIp, login));
                } else {
                    throw new HttpProcessingException(HttpURLConnection.HTTP_CONFLICT); // 409
                }
            } catch (LoginInvalidException exp) {
                logHttpError(LOGGER, HttpURLConnection.HTTP_BAD_REQUEST, request.uri(), exp);
                throw new HttpProcessingException(HttpURLConnection.HTTP_BAD_REQUEST); // 400
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }
}