package com.gempukku.stccg.service;

import com.gempukku.stccg.db.IpBanDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;

import java.sql.SQLException;

public class AdminService {
    private static final int DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private final PlayerDAO _playerDAO;
    private final LoggedUserHolder _loggedUserHolder;
    private final IpBanDAO _ipBanDAO;

    public AdminService(PlayerDAO playerDAO, IpBanDAO ipBanDAO, LoggedUserHolder loggedUserHolder) {
        _playerDAO = playerDAO;
        _ipBanDAO = ipBanDAO;
        _loggedUserHolder = loggedUserHolder;
    }

    public final void resetUserPassword(String login) throws SQLException {
        final boolean success = _playerDAO.resetUserPassword(login);
        if (!success)
            throw new SQLException();
        _loggedUserHolder.forceLogoutUser(login);
    }

    public final boolean banUser(String login) {
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

    public final boolean banUserTemp(String login, int days) {
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

    public final boolean unBanUser(String login) {
        try {
            return _playerDAO.unBanPlayer(login);
        } catch (SQLException exp) {
            return false;
        }
    }

    public final void banIp(String login) {
        final User player = _playerDAO.getPlayer(login);
        if (player == null)
            return;
        final String lastIp = player.getLastIp();
        
        _ipBanDAO.addIpBan(lastIp);

        banUser(login);
    }

    public final void banIpPrefix(String login) {
        final User player = _playerDAO.getPlayer(login);
        if (player == null)
            return;
        player.banIpPrefix(_ipBanDAO);
        banUser(login);
    }
}