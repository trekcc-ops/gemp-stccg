package com.gempukku.stccg.database;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.async.LoggingProxy;

import java.util.HashSet;
import java.util.Set;

public class CachedIpBanDAO implements IpBanDAO, Cached {
    private final IpBanDAO _delegate;
    private final Set<String> _bannedIps = new HashSet<>();
    private final Set<String> _bannedIpPrefixes = new HashSet<>();

    public CachedIpBanDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(IpBanDAO.class, new DbIpBanDAO(dbAccess));
    }

    @Override
    public final void clearCache() {
        _bannedIps.clear();
        _bannedIpPrefixes.clear();
    }

    @Override
    public final int getItemCount() {
        return _bannedIps.size() + _bannedIpPrefixes.size();
    }

    @Override
    public final void addIpBan(String ip) {
        _delegate.addIpBan(ip);
        _bannedIps.clear();
    }

    @Override
    public final void addIpPrefixBan(String ipPrefix) {
        _delegate.addIpPrefixBan(ipPrefix);
        _bannedIpPrefixes.clear();
    }

    @Override
    public boolean isIpBanned(String ip) {
        return _delegate.isIpBanned(ip);
    }

    @Override
    public final Set<String> getIpBans() {
        if (_bannedIps.isEmpty()) {
            _bannedIps.addAll(_delegate.getIpBans());
        }
        return _bannedIps;
    }

    @Override
    public final Set<String> getIpPrefixBans() {
        if (_bannedIpPrefixes.isEmpty()) {
            _bannedIpPrefixes.addAll(_delegate.getIpPrefixBans());
        }
        return _bannedIpPrefixes;
    }
}