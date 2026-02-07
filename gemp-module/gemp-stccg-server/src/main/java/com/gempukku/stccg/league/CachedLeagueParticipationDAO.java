package com.gempukku.stccg.league;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.database.DbAccess;
import com.gempukku.stccg.database.DbLeagueParticipationDAO;
import com.gempukku.stccg.database.LeagueParticipationDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.async.LoggingProxy;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachedLeagueParticipationDAO implements LeagueParticipationDAO, Cached {
    private final LeagueParticipationDAO _leagueParticipationDAO;
    private final ReadWriteLock _readWriteLock = new ReentrantReadWriteLock();

    private final Map<Integer, Set<String>> _cachedParticipants = Collections.synchronizedMap(new LRUMap<>(5));

    public CachedLeagueParticipationDAO(DbAccess dbAccess) {
        _leagueParticipationDAO =
                LoggingProxy.createLoggingProxy(LeagueParticipationDAO.class, new DbLeagueParticipationDAO(dbAccess));
    }

    public CachedLeagueParticipationDAO(LeagueParticipationDAO leagueParticipationDAO) {
        _leagueParticipationDAO = leagueParticipationDAO;
    }

    @Override
    public void userJoinsLeague(int leagueId, User player, String remoteAddress) {
        _readWriteLock.writeLock().lock();
        try {
            getLeagueParticipantsInWriteLock(leagueId).add(player.getName());
            _leagueParticipationDAO.userJoinsLeague(leagueId, player, remoteAddress);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<String> getUsersParticipating(int leagueId) {
        _readWriteLock.readLock().lock();
        try {
            Collection<String> leagueParticipants = _cachedParticipants.get(leagueId);
            if (leagueParticipants == null) {
                _readWriteLock.readLock().unlock();
                _readWriteLock.writeLock().lock();
                try {
                    leagueParticipants = getLeagueParticipantsInWriteLock(leagueId);
                } finally {
                    _readWriteLock.readLock().lock();
                    _readWriteLock.writeLock().unlock();
                }
            }
            return Collections.unmodifiableCollection(leagueParticipants);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }


    private Collection<String> getLeagueParticipantsInWriteLock(int leagueId) {
        Set<String> leagueParticipants;
        leagueParticipants = _cachedParticipants.get(leagueId);
        if (leagueParticipants == null) {
            leagueParticipants = new CopyOnWriteArraySet<>(_leagueParticipationDAO.getUsersParticipating(leagueId));
            _cachedParticipants.put(leagueId, leagueParticipants);
        }
        return leagueParticipants;
    }

    @Override
    public int getItemCount() {
        return _cachedParticipants.size();
    }

    @Override
    public void clearCache() {
        _readWriteLock.writeLock().lock();
        try {
            _cachedParticipants.clear();
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }
}