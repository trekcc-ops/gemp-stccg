package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.HttpProcessingException;

import java.net.HttpURLConnection;

public class PrivateInformationException extends HttpProcessingException {

    public PrivateInformationException() {
        super(HttpURLConnection.HTTP_FORBIDDEN); // 403
    }
}