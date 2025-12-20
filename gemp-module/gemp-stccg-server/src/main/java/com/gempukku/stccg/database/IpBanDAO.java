package com.gempukku.stccg.database;

import java.util.Set;

public interface IpBanDAO {
    Set<String> getIpBans();
    Set<String> getIpPrefixBans();
    void addIpBan(String ip);
    void addIpPrefixBan(String ipPrefix);
    boolean isIpBanned(String ip);
}