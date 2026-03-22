package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.GempHttpRequest;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;

public class WebRequestHandler {
    private final String _root;

    public WebRequestHandler() {
        this(AppConfig.getWebPath());
    }

    private WebRequestHandler(String root) {
        _root = root;
    }

    public final void handleRequest(String uri, GempHttpRequest request, ResponseWriter responseWriter)
            throws Exception {
        if (uri.isEmpty())
            uri = "index.html";

        uri = uri.replace('/', File.separatorChar);

        if ((uri.contains(".."))
                || uri.contains(File.separator + ".")
                || uri.startsWith(".") || uri.endsWith("."))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403

        File file = new File(_root + uri);
        if (!file.getCanonicalPath().startsWith(_root))
            throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403

        if (!file.exists())
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404

        final String eTag = "\""+file.lastModified()+"\"";

        if (clientHasCurrentVersion(request, eTag))
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_MODIFIED); // 304

        responseWriter.writeFile(file, Collections.singletonMap(HttpHeaderNames.ETAG.toString(), eTag));
    }

    private static boolean clientHasCurrentVersion(GempHttpRequest request, String eTag) {
        String ifNoneMatch = request.headers().get(HttpHeaderNames.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            String[] clientKnownVersions = ifNoneMatch.split(",");
            for (String clientKnownVersion : clientKnownVersions) {
                if (clientKnownVersion.trim().equals(eTag))
                    return true;
            }
        }
        return false;
    }
}