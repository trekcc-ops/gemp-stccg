package com.gempukku.stccg.service;

import com.gempukku.stccg.TextUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoggedUserHolder {

    private final static long SESSION_LENGTH = 1000 * 60 * 10;
    private final static long EXPIRE_CHECK_INTERVAL = 1000 * 60;
    private final static int SESSION_ID_LENGTH = 20;
    private final Map<String, String> _sessionIdsToUsers = new HashMap<>();
    private final Multimap<String, String> _usersToSessionIds = HashMultimap.create();

    private final Map<String, Long> _lastAccess = Collections.synchronizedMap(new HashMap<>());
    private final ReadWriteLock _readWriteLock = new ReentrantReadWriteLock();

    public final void start() {
        ClearExpiredRunnable runnable = new ClearExpiredRunnable();
        Thread thr = new Thread(runnable);
        thr.start();
    }

    public final String getLoggedUser(String sessionId) {
        _readWriteLock.readLock().lock();
        try {
            String loggedUser = _sessionIdsToUsers.get(sessionId);
            if (loggedUser != null) {
                _lastAccess.put(sessionId, System.currentTimeMillis());
                return loggedUser;
            }
        } finally {
            _readWriteLock.readLock().unlock();
        }
        return null;
    }

    public final String getLoggedUser(HttpMessage request) {
        ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
        HttpHeaders headers = request.headers();
        String cookieHeader = headers.get(HttpHeaderNames.COOKIE);
        if (cookieHeader != null) {
            Set<Cookie> cookies = cookieDecoder.decode(cookieHeader);
            for (Cookie cookie : cookies) {
                String name = cookie.name();
                if ("loggedUser".equals(name)) {
                    String value = cookie.value();
                    if (value != null) {
                        return getLoggedUser(value);
                    }
                }
            }
        }
        return null;
    }


    public final String logUser(String userName) {
        _readWriteLock.writeLock().lock();
        try {
            String userValue = insertValueForUser(userName);
            _lastAccess.put(userValue, System.currentTimeMillis());
            return userValue;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public final void forceLogoutUser(String userName) {
        _readWriteLock.writeLock().lock();
        try {
            final Iterable<String> sessionIds = new HashSet<>(_usersToSessionIds.get(userName));
            for (String sessionId : sessionIds) {
                _sessionIdsToUsers.remove(sessionId);
                _usersToSessionIds.remove(userName, sessionId);
            }
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    private final char[] _chars = TextUtils.getAllCharacters(true, false).toCharArray();

    private String insertValueForUser(String userName) {
        Random rnd = ThreadLocalRandom.current();
        String sessionId;
        do {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < SESSION_ID_LENGTH; i++)
                result.append(_chars[rnd.nextInt(_chars.length)]);
            sessionId = result.toString();
        } while (_sessionIdsToUsers.containsKey(sessionId));
        _sessionIdsToUsers.put(sessionId, userName);
        _usersToSessionIds.put(userName, sessionId);
        return sessionId;
    }

    private class ClearExpiredRunnable implements Runnable {
        @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
        @Override
        public final void run() {
            while (true) {
                _readWriteLock.writeLock().lock();
                try {
                    long currentTime = System.currentTimeMillis();
                    Iterator<Map.Entry<String, Long>> iterator = _lastAccess.entrySet().iterator();
                    if (iterator.hasNext()) {
                        Map.Entry<String, Long> lastAccess = iterator.next();
                        long expireAt = lastAccess.getValue() + SESSION_LENGTH;
                        if (expireAt < currentTime) {
                            String sessionId = lastAccess.getKey();
                            final String userName = _sessionIdsToUsers.remove(sessionId);
                            if (userName != null)
                                _usersToSessionIds.remove(userName, sessionId);
                            iterator.remove();
                        }
                    }
                } finally {
                    _readWriteLock.writeLock().unlock();
                }
                try {
                    Thread.sleep(EXPIRE_CHECK_INTERVAL);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }
}