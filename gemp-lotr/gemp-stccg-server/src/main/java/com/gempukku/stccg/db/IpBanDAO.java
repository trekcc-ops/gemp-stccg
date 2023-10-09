package com.gempukku.stccg.db;

import java.util.Set;

public interface IpBanDAO {
    Set<String> getIpBans();
    Set<String> getIpPrefixBans();
    void addIpBan(String ip);
    void addIpPrefixBan(String ipPrefix);
}
