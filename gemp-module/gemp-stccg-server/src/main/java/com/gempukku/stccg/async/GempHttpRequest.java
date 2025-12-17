package com.gempukku.stccg.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.gempukku.stccg.async.handler.ResponseWriter;
import com.gempukku.stccg.async.handler.UriRequestHandler;
import com.gempukku.stccg.async.handler.WebRequestHandler;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.database.IpBanDAO;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.database.UserNotFoundException;
import com.gempukku.stccg.service.LoggedUserHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;

public class GempHttpRequest {

    private static final Logger LOGGER = LogManager.getLogger(GempHttpRequest.class);
    private static final String SERVER_CONTEXT_PATH = "/gemp-stccg-server/";
    private static final String WEB_CONTEXT_PATH = "/gemp-module/";
    private final Pattern originPattern = Pattern.compile(AppConfig.getProperty("origin.allowed.pattern"));

    private final FullHttpRequest _request;
    private final LoggedUserHolder _loggedUserHolder;
    private final String _ipAddress;
    private final boolean _ipBanned;
    private final User _user;
    private final ResponseWriter _responseWriter;

    public GempHttpRequest(FullHttpRequest request, ChannelHandlerContext context, ServerObjects serverObjects) {
        _request = request;
        _loggedUserHolder = serverObjects.getLoggedUserHolder();
        _ipAddress = Objects.requireNonNullElse(request.headers().get("X-Forwarded-For"),
                ((InetSocketAddress) context.channel().remoteAddress()).getAddress().getHostAddress()
        );
        _ipBanned = determineIfIpIsBanned(serverObjects);
        _user = identifyUser(serverObjects);
        _responseWriter = new ResponseSender(context, request);
    }

    private boolean determineIfIpIsBanned(ServerObjects serverObjects) {
        IpBanDAO ipBanDAO = serverObjects.getIpBanDAO();
        if (ipBanDAO.getIpBans().contains(_ipAddress))
            return true;
        for (String bannedRange : ipBanDAO.getIpPrefixBans()) {
            if (_ipAddress.startsWith(bannedRange))
                return true;
        }
        return false;
    }

    public HttpHeaders headers() {
        return _request.headers();
    }

    public HttpMethod method() {
        return _request.method();
    }

    private User identifyUser(ServerObjects serverObjects) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        String cookieHeader = _request.headers().get(COOKIE);
        String userName = "";
        PlayerDAO playerDAO = serverObjects.getPlayerDAO();

        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                if ("loggedUser".equals(cookie.name())) {
                    String value = cookie.value();
                    if (value != null) {
                        userName = _loggedUserHolder.getLoggedUserNew(value);
                    }
                }
            }
        }
        if (!userName.isEmpty()) {
            try {
                return playerDAO.getPlayer(userName);
            } catch(UserNotFoundException exp) {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean bannedIp() {
        return _ipBanned;
    }

    public String ip() {
        return _ipAddress;
    }

    public String uri() {
        return _request.uri();
    }

    public User user() throws HttpProcessingException {
        if (_user == null) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        } else {
            return _user;
        }
    }

    public String userName() throws HttpProcessingException {
        if (_user == null) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED); // 401
        } else {
            return _user.getName();
        }
    }


    Map<String, Object> parameters() throws IOException {
        Map<String, List<String>> lists = new HashMap<>();
        Map<String, String> items = new HashMap<>();
        if (_request.method() == HttpMethod.POST) {
            InterfaceHttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(_request);
            try {
                for (InterfaceHttpData data : postDecoder.getBodyHttpDatas()) {
                    if (data instanceof Attribute attribute) {
                        if (attribute.getName().endsWith("[]")) {
                            String attributeName = attribute.getName().replace("[]","");
                            lists.computeIfAbsent(attributeName, k -> new ArrayList<>());
                            lists.get(attributeName).add(attribute.getValue());
                        } else {
                            items.put(attribute.getName(), attribute.getValue());
                        }
                    }
                }
            } finally {
                postDecoder.destroy();
            }
        } else if (_request.method() == HttpMethod.GET) {
            QueryStringDecoder queryDecoder = new QueryStringDecoder(_request.uri());
            for (Map.Entry<String, List<String>> entry : queryDecoder.parameters().entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty() && !entry.getKey().equals("_")) {
                    items.put(entry.getKey(), entry.getValue().getFirst());
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.putAll(items);
        result.putAll(lists);
        return result;
    }

    public Map<String, Object> parametersWithType(String handlerType) throws IOException {
        Map<String, Object> parameters = parameters();
        parameters.put("type", handlerType);
        return parameters;
    }

    public HttpRequest getRequest() {
        return _request;
    }

    public String uriWithoutParameters() {
        String uri = _request.uri();
        if (uri.contains("?"))
            uri = uri.substring(0, uri.indexOf('?'));
        return uri;
    }

    public String cookieHeader() {
        String result = _request.headers().get(HttpHeaderNames.COOKIE);
        return (result == null) ? "" : result;
    }

    public void handle(ServerObjects serverObjects) {
        try {
            if (bannedIp()) { // throw 401 error
                throw new HttpProcessingException(HttpURLConnection.HTTP_UNAUTHORIZED,
                        "Denying entry to user from banned IP " + ip());
            } else {
                String uri = uriWithoutParameters();

                if (uri.startsWith(WEB_CONTEXT_PATH)) {
                    new WebRequestHandler().handleRequest(uri.substring(WEB_CONTEXT_PATH.length()), this, _responseWriter);
                } else if (uri.replace("/", "").isEmpty() ||
                        uri.replace("/", "").equals(WEB_CONTEXT_PATH.replace("/", ""))) {
                    // 301 Moved Permanently
                    _responseWriter.writeError(
                            HttpURLConnection.HTTP_MOVED_PERM, Collections.singletonMap("Location", WEB_CONTEXT_PATH));
                } else if (uri.startsWith(SERVER_CONTEXT_PATH)) {
                    handleServerRequest(serverObjects);
                }
            }
        } catch (HttpProcessingException exp) {
            logHttpError(uri(), exp);
        } catch (Exception exp) {
            LOGGER.error("Error response for {}", uri(), exp);
            _responseWriter.writeError(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500
        }
    }

    private void logHttpError(String uri, HttpProcessingException exp) {
        int code = exp.getStatus();
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 (HTTP_BAD_REQUEST) itself should error out
        if(code < 500 && code != HttpURLConnection.HTTP_BAD_REQUEST)
            LOGGER.debug("HTTP {} response for {}", code, uri);

            // record an HTTP 400 or 500 error
        else if((code < 600))
            LOGGER.error("HTTP code {} response for {}", code, uri, exp);
        _responseWriter.writeError(exp.getStatus());
    }

    private void validateOrigin(GempHttpRequest request) throws HttpProcessingException {
        String origin = request.headers().get("Origin");
        if (origin != null) {
            if (!originPattern.matcher(origin).matches())
                throw new HttpProcessingException(HttpURLConnection.HTTP_FORBIDDEN); // 403
        }
    }

    private void handleServerRequest(ServerObjects serverObjects) throws Exception {
        String uri = uriWithoutParameters();
        validateOrigin(this);

        String afterServer = uri.substring(SERVER_CONTEXT_PATH.length());
        int nextSlashIndex = afterServer.indexOf("/");
        String handlerType = (nextSlashIndex < 0) ? afterServer : afterServer.substring(0, nextSlashIndex);
        Map<String, Object> parameters = parametersWithType(handlerType);

        if (handlerType.equals("collection")) {
            String afterHandlerType = afterServer.substring(handlerType.length());
            String collectionType = afterHandlerType.substring(1);
            parameters.put("collectionType", collectionType);
        }

        try {
            UriRequestHandler newHandler = new ObjectMapper().convertValue(parameters, UriRequestHandler.class);
            newHandler.handleRequest(this, _responseWriter, serverObjects);
        } catch (IllegalArgumentException exp) {
            if (exp.getCause() instanceof InvalidTypeIdException) {
                // InvalidTypeIdException thrown if initial path not recognized by the Json deserializer
                logHttpError(uri,
                        new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND));
            }
        } catch (JsonProcessingException exp) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        }
    }

}