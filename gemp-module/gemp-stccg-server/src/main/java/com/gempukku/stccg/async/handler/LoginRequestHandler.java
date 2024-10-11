package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.db.User;
import com.mysql.cj.util.StringUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.util.Date;

public class LoginRequestHandler extends DefaultServerRequestHandler implements UriRequestHandler {

    public LoginRequestHandler(ServerObjects objects) {
        super(objects);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty() && request.method() == HttpMethod.POST) {
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
            try {
            String login = getFormParameterSafely(postDecoder, "login");
            String password = getFormParameterSafely(postDecoder, "password");

            User player = _playerDao.loginUser(login, password);
            if (player != null) {
                if (StringUtils.isNullOrEmpty(player.getPassword())) {
                    throw new HttpProcessingException(202);
                }
                if (player.getType().contains(User.Type.USER.getValue())) {
                    final Date bannedUntil = player.getBannedUntil();
                    if (bannedUntil != null && bannedUntil.after(new Date()))
                        throw new HttpProcessingException(409);
                    else
                        responseWriter.writeXmlResponse(null, logUserReturningHeaders(remoteIp, login));
                } else {
                    throw new HttpProcessingException(403);
                }
            } else {
                throw new HttpProcessingException(401);
            }
            } finally {
                postDecoder.destroy();
            }
        } else {
            throw new HttpProcessingException(404);
        }
    }

}