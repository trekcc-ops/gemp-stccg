package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;

public class WebRequestHandler implements UriRequestHandler {
    private final String _root;

    public WebRequestHandler() {
        this(AppConfig.getWebPath());
    }

    private WebRequestHandler(String root) {
        _root = root;
    }

    @Override
    public final void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
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

    private static boolean clientHasCurrentVersion(HttpMessage request, String eTag) {
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