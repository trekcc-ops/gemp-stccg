package com.gempukku.stccg.db;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.async.LoggingProxy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachedIgnoreDAO implements IgnoreDAO, Cached {
    private final Map<String, Set<String>> ignores = new ConcurrentHashMap<>();
    private final IgnoreDAO delegate;

    public CachedIgnoreDAO(DbAccess dbAccess) {
        this.delegate = LoggingProxy.createLoggingProxy(IgnoreDAO.class, new DbIgnoreDAO(dbAccess));
    }
    @Override
    public final void clearCache() {
        ignores.clear();
    }

    @Override
    public final int getItemCount() {
        return ignores.size();
    }

    @Override
    public final Set<String> getIgnoredUsers(String playerId) {
        Set<String> ignoredUsers = ignores.get(playerId);
        if (ignoredUsers == null) {
            ignoredUsers = Collections.synchronizedSet(delegate.getIgnoredUsers(playerId));
            ignores.put(playerId, ignoredUsers);
        }
        return ignoredUsers;
    }

    @Override
    public final boolean addIgnoredUser(String playerId, String ignoredName) {
        final Set<String> ignoredUsers = getIgnoredUsers(playerId);
        if (!ignoredUsers.contains(ignoredName)) {
            delegate.addIgnoredUser(playerId, ignoredName);
            ignoredUsers.add(ignoredName);
            return true;
        }
        return false;
    }

    @Override
    public final boolean removeIgnoredUser(String playerId, String ignoredName) {
        final Set<String> ignoredUsers = getIgnoredUsers(playerId);
        if (ignoredUsers.contains(ignoredName)) {
            delegate.removeIgnoredUser(playerId, ignoredName);
            ignoredUsers.remove(ignoredName);
            return true;
        }
        return false;
    }
}