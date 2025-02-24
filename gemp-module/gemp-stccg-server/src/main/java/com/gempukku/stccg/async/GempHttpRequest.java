package com.gempukku.stccg.async;

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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;

public class GempHttpRequest {

    private final FullHttpRequest _request;
    private final LoggedUserHolder _loggedUserHolder;
    private final String _ipAddress;
    private final boolean _ipBanned;
    private final User _user;

    public GempHttpRequest(FullHttpRequest request, ChannelHandlerContext context, ServerObjects serverObjects) {
        _request = request;
        _loggedUserHolder = serverObjects.getLoggedUserHolder();
        _ipAddress = Objects.requireNonNullElse(request.headers().get("X-Forwarded-For"),
                ((InetSocketAddress) context.channel().remoteAddress()).getAddress().getHostAddress()
        );
        _ipBanned = determineIfIpIsBanned(serverObjects);
        _user = identifyUser(serverObjects);
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

}