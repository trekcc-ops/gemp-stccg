package com.gempukku.stccg.async.handler;

import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;

public final class HttpUtils {

    public static void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code % 400 < 100 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            log.debug("HTTP " + code + " response for " + uri);

            // record an HTTP 400
        else if(code == HttpURLConnection.HTTP_BAD_REQUEST || code % 500 < 100)
            log.error("HTTP code " + code + " response for " + uri, exp);
    }

}