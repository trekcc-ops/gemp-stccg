package com.gempukku.stccg;

import com.gempukku.stccg.async.HttpProcessingException;

import java.net.HttpURLConnection;

public class SubscriptionConflictException extends HttpProcessingException {

    public SubscriptionConflictException() {
        super(HttpURLConnection.HTTP_CONFLICT); // 409
    }
}