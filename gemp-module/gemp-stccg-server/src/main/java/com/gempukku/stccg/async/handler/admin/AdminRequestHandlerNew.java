package com.gempukku.stccg.async.handler.admin;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.database.User;
import io.netty.handler.codec.http.HttpMessage;

import java.net.HttpURLConnection;

public abstract class AdminRequestHandlerNew {

    void validateAdmin(GempHttpRequest request) throws HttpProcessingException {
        User user = request.user();
        if (!user.isAdmin())
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }

    void validateLeagueAdmin(GempHttpRequest request) throws HttpProcessingException {
        User user = request.user();
        if (!user.isLeagueAdmin())
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }



}