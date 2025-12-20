package com.gempukku.stccg.database;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.async.LoggingProxy;

import java.util.Set;

public class CachedIpBanDAO implements IpBanDAO, Cached {
    private final IpBanDAO _delegate;
    private Set<String> _bannedIps;
    private Set<String> _bannedIpPrefixes;

    public CachedIpBanDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(IpBanDAO.class, new DbIpBanDAO(dbAccess));
    }

    @Override
    public final void clearCache() {
        _bannedIps = null;
        _bannedIpPrefixes = null;
    }

    @Override
    public final int getItemCount() {
        int total = 0;
        if(_bannedIps != null)
            total += _bannedIps.size();
        if(_bannedIpPrefixes != null)
            total += _bannedIpPrefixes.size();
        return total;
    }

    @Override
    public final void addIpBan(String ip) {
        _delegate.addIpBan(ip);
        _bannedIps = null;
    }

    @Override
    public final void addIpPrefixBan(String ipPrefix) {
        _delegate.addIpPrefixBan(ipPrefix);
        _bannedIpPrefixes = null;
    }

    @Override
    public boolean isIpBanned(String ip) {
        return _delegate.isIpBanned(ip);
    }

    @Override
    public final Set<String> getIpBans() {
        Set<String> result = _bannedIps;
        if (result != null)
            return result;

        result = _delegate.getIpBans();
        _bannedIps = result;
        return result;
    }

    @Override
    public final Set<String> getIpPrefixBans() {
        Set<String> result = _bannedIpPrefixes;
        if (result != null)
            return result;

        result = _delegate.getIpPrefixBans();
        _bannedIpPrefixes = result;
        return result;
    }
}