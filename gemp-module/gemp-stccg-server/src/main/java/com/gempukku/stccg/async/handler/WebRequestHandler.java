package com.gempukku.stccg.async.handler;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ResponseWriter;
import com.gempukku.stccg.common.AppConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.util.Collections;

public class WebRequestHandler implements UriRequestHandler {
    private final String _root;

    public WebRequestHandler() {
        this(AppConfig.getWebPath());
    }

    public WebRequestHandler(String root) {
        _root = root;
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.isEmpty())
            uri = "index.html";

        uri = uri.replace('/', File.separatorChar);

        if ((uri.contains(".."))
                || uri.contains(File.separator + ".")
                || uri.startsWith(".") || uri.endsWith("."))
            throw new HttpProcessingException(403);

        File file = new File(_root + uri);
        if (!file.getCanonicalPath().startsWith(_root))
            throw new HttpProcessingException(403);

        if (!file.exists())
            throw new HttpProcessingException(404);

        final String eTag = "\""+file.lastModified()+"\"";

        if (clientHasCurrentVersion(request, eTag))
            throw new HttpProcessingException(304);

        responseWriter.writeFile(file, Collections.singletonMap(HttpHeaderNames.ETAG.toString(), eTag));
    }

    private boolean clientHasCurrentVersion(HttpRequest request, String eTag) {
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