package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.db.LoginInvalidException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegisterRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(RegisterRequestHandler.class);
    public RegisterRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
            String login = getFormParameterSafely(postDecoder, "login");
            String password = getFormParameterSafely(postDecoder, "password");
            try {
                if (_playerDao.registerUser(login, password, remoteIp)) {
                    responseWriter.writeXmlResponse(null, logUserReturningHeaders(remoteIp, login));
                } else {
                    throw new HttpProcessingException(409);
                }
            } catch (LoginInvalidException exp) {
                logHttpError(LOGGER, 400, request.uri(), exp);
                throw new HttpProcessingException(400);
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }
}