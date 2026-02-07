package com.gempukku.stccg.service;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.database.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class AdminService {
    public static final int DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private final PlayerDAO _playerDAO;
    private final LoggedUserHolder _loggedUserHolder;
    private final IpBanDAO _ipBanDAO;
    private final IgnoreDAO _ignoreDAO;

    public AdminService(PlayerDAO playerDAO, IpBanDAO ipBanDAO, DbAccess dbAccess) {
        _playerDAO = playerDAO;
        _ipBanDAO = ipBanDAO;
        _ignoreDAO = new CachedIgnoreDAO(dbAccess);
        _loggedUserHolder = new LoggedUserHolder();
        _loggedUserHolder.start();
    }

    public void resetUserPassword(String login) throws SQLException {
        boolean success = _playerDAO.resetUserPassword(login);
        _loggedUserHolder.forceLogoutUser(login);
        if (!success) {
            throw new SQLException("Unable to reset user password");
        }
    }

    public boolean banUser(String login) {
        try {
            final boolean success = _playerDAO.banPlayerPermanently(login);
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
        }
    }

    public boolean banUserTemp(String login, int days) {
        try {
            final boolean success = _playerDAO.banPlayerTemporarily(
                    login, System.currentTimeMillis() + (long) days * DAY_IN_MILLIS
            );
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
        }
    }

    public boolean unBanUser(String login) {
        try {
            return _playerDAO.unBanPlayer(login);
        } catch (SQLException exp) {
            return false;
        }
    }

    public void banIp(String login) throws UserNotFoundException {
        final String lastIp = _playerDAO.getLastIpForUserName(login);
        _ipBanDAO.addIpBan(lastIp);
        banUser(login);
    }

    public void banIpPrefix(String login) throws UserNotFoundException {
        final String lastIp = _playerDAO.getLastIpForUserName(login);
        String lastIpPrefix = lastIp.substring(0, lastIp.lastIndexOf(".")+1);
        _ipBanDAO.addIpPrefixBan(lastIpPrefix);
        banUser(login);
    }

    public void addPlayerFlag(String userName, User.Type type) throws HttpProcessingException {
        try {
            _playerDAO.addPlayerFlag(userName, type);
        } catch(SQLException exp) {
            throw new HttpProcessingException(HTTP_NOT_FOUND, exp.getMessage());
        }
    }

    public void removePlayerFlag(String userName, User.Type type) throws HttpProcessingException {
        try {
            _playerDAO.removePlayerFlag(userName, type);
        } catch(SQLException exp) {
            throw new HttpProcessingException(HTTP_NOT_FOUND, exp.getMessage());
        }
    }

    public boolean registerUser(String userId, String password, String ip)
            throws HttpProcessingException, LoginInvalidException {
        try {
            return _playerDAO.registerUser(userId, password, ip);
        } catch(SQLException exp) {
            throw new HttpProcessingException(HTTP_NOT_FOUND, exp.getMessage());
        }
    }

    public void updateLastLoginIp(String login, String remoteIp) throws SQLException {
        _playerDAO.updateLastLoginIp(login, remoteIp);
    }

    public User loginUser(String userId, String password) {
        return _playerDAO.loginUser(userId, password);
    }

    public User getPlayer(String userName) throws UserNotFoundException {
        return _playerDAO.getPlayer(userName);
    }

    public String logUser(String login) {
        return _loggedUserHolder.logUser(login);
    }

    public String getLoggedUserNew(String sessionId) {
        return _loggedUserHolder.getLoggedUserNew(sessionId);
    }

    public boolean isIpBanned(String ip) {
        return _ipBanDAO.isIpBanned(ip);
    }

    public Set<String> getBannedUsernames() throws SQLException {
        return _playerDAO.getBannedUsernames();
    }

    public Set<String> getIgnoredUsers(String playerId) {
        return _ignoreDAO.getIgnoredUsers(playerId);
    }

    public boolean addIgnoredUser(String from, String playerName) {
        return _ignoreDAO.addIgnoredUser(from, playerName);
    }

    public boolean removeIgnoredUser(String from, String playerName) {
        return _ignoreDAO.removeIgnoredUser(from, playerName);
    }

    public List<User> findSimilarAccounts(String userName) throws SQLException {
        return _playerDAO.findSimilarAccounts(userName);
    }
}