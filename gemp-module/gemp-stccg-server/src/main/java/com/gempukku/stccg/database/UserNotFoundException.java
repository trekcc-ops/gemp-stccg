package com.gempukku.stccg.database;

import com.gempukku.stccg.async.HttpProcessingException;

import javax.net.ssl.HttpsURLConnection;

public class UserNotFoundException extends HttpProcessingException {

    public UserNotFoundException(String message) {
        super(HttpsURLConnection.HTTP_NOT_FOUND, message);
    }
}