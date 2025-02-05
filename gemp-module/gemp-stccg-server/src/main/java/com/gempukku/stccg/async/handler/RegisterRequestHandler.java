package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.LoginInvalidException;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.util.Map;

public class RegisterRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    private static final Logger LOGGER = LogManager.getLogger(RegisterRequestHandler.class);
    RegisterRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp)
            throws Exception {
        if (!uri.isEmpty() || request.method() != HttpMethod.POST)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        try(SelfClosingPostRequestDecoder postDecoder = new SelfClosingPostRequestDecoder(request)) {
            String login = getFormParameterSafely(postDecoder, FormParameter.login);
            String password = getFormParameterSafely(postDecoder, FormParameter.password);
            if (_playerDao.registerUser(login, password, remoteIp)) {
                Map<String, String> headers = logUserReturningHeaders(remoteIp, login);
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