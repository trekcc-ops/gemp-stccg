package com.gempukku.stccg.database;

import java.util.Set;

public interface IgnoreDAO {
    Set<String> getIgnoredUsers(String playerId);

    boolean addIgnoredUser(String playerId, String ignoredName);

    boolean removeIgnoredUser(String playerId, String ignoredName);
}