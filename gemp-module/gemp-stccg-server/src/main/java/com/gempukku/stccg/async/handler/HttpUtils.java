package com.gempukku.stccg.async.handler;

import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public final class HttpUtils {

    public static void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code < 500 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            log.debug("HTTP {} response for {}", code, uri);

            // record an HTTP 400 or 500 error
        else if((code < 600))
            log.error("HTTP code {} response for {}", code, uri, exp);
    }

}