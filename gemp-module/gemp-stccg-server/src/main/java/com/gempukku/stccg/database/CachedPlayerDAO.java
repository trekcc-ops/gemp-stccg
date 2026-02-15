package com.gempukku.stccg.database;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.async.LoggingProxy;
import org.apache.commons.collections4.map.LRUMap;

import java.sql.SQLException;
import java.util.*;

public class CachedPlayerDAO implements PlayerDAO, Cached {
    private final static int MAX_MAP_SIZE = 500;
    private final PlayerDAO _delegate;
    private final Map<Integer, User> _playerById = Collections.synchronizedMap(new LRUMap<>(MAX_MAP_SIZE));
    private final Map<String, User> _playerByName = Collections.synchronizedMap(new LRUMap<>(MAX_MAP_SIZE));
    private Set<String> _bannedUsernames = new HashSet<>();

    public CachedPlayerDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(PlayerDAO.class, new DbPlayerDAO(dbAccess));
    }

    @Override
    public final void clearCache() {
        _playerById.clear();
        _playerByName.clear();
        _bannedUsernames.clear();
    }

    @Override
    public final int getItemCount() {
        return _playerById.size() + _playerByName.size();
    }

    @Override
    public final boolean resetUserPassword(String login) throws SQLException {
        final boolean success = _delegate.resetUserPassword(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final boolean banPlayerPermanently(String login) throws SQLException {
        final boolean success = _delegate.banPlayerPermanently(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final boolean banPlayerTemporarily(String login, long dateTo) throws SQLException {
        final boolean success = _delegate.banPlayerTemporarily(login, dateTo);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final boolean unBanPlayer(String login) throws SQLException {
        final boolean success = _delegate.unBanPlayer(login);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final boolean addPlayerFlag(String login, User.Type flag) throws SQLException {
        final boolean success = _delegate.addPlayerFlag(login, flag);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final boolean removePlayerFlag(String login, User.Type flag) throws SQLException {
        final boolean success = _delegate.removePlayerFlag(login, flag);
        if (success)
            clearCache();
        return success;
    }

    @Override
    public final List<User> findSimilarAccounts(String login) throws SQLException {
        return _delegate.findSimilarAccounts(login);
    }

    @Override
    public final Set<String> getBannedUsernames() throws SQLException {
        if(_bannedUsernames.isEmpty())
            _bannedUsernames = _delegate.getBannedUsernames();
        return new HashSet<>(_bannedUsernames);
    }

    @Override
    public final User getPlayer(int id) {
        User player = _playerById.get(id);
        if (player == null) {
            player = _delegate.getPlayer(id);
            if (player != null) {
                _playerById.put(id, player);
                _playerByName.put(player.getName(), player);
            }
        }
        return player;
    }

    @Override
    public final User getPlayer(String playerName) throws UserNotFoundException {
        User player = _playerByName.get(playerName);
        if (player == null) {
            player = _delegate.getPlayer(playerName);
            if (player != null) {
                int playerId = player.getId();
                _playerById.put(playerId, player);
                _playerByName.put(playerName, player);
                return player;
            }
        }
        if (player != null)
            return player;
        else
            throw new UserNotFoundException("Unable to find user '" + playerName + "'");
    }

    @Override
    public final User loginUser(String login, String password) {
        return _delegate.loginUser(login, password);
    }

    @Override
    public final boolean registerUser(String login, String password, String remoteAddress)
            throws SQLException, LoginInvalidException {
        boolean registered = _delegate.registerUser(login, password, remoteAddress);
        if (registered)
            _playerByName.remove(login);
        return registered;
    }

    @Override
    public final void setLastReward(User player, int currentReward) throws SQLException {
        _delegate.setLastReward(player, currentReward);
        _playerById.remove(player.getId());
        _playerByName.remove(player.getName());
    }

    @Override
    public final void updateLastLoginIp(String login, String remoteAddress) throws SQLException {
        _delegate.updateLastLoginIp(login, remoteAddress);
    }

    public final List<DBData.DBPlayer> getAllPlayers() {
        return _delegate.getAllPlayers();
    }

    @Override
    public final boolean updateLastReward(User player, int previousReward, int currentReward) throws SQLException {
        boolean updated = _delegate.updateLastReward(player, previousReward, currentReward);
        if (updated) {
            _playerById.remove(player.getId());
            _playerByName.remove(player.getName());
        }
        return updated;
    }
}