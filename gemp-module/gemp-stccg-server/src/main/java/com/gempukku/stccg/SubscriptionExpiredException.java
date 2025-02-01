package com.gempukku.stccg;

import com.gempukku.stccg.async.HttpProcessingException;

import java.net.HttpURLConnection;

public class SubscriptionExpiredException extends HttpProcessingException {

    public SubscriptionExpiredException() {
        super(HttpURLConnection.HTTP_GONE);
    }
}