package com.gempukku.stccg.service;

import com.gempukku.stccg.db.IpBanDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;

import java.sql.SQLException;

public class AdminService {
    public static final int DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private final PlayerDAO _playerDAO;
    private final LoggedUserHolder _loggedUserHolder;
    private final IpBanDAO _ipBanDAO;

    public AdminService(PlayerDAO playerDAO, IpBanDAO ipBanDAO, LoggedUserHolder loggedUserHolder) {
        _playerDAO = playerDAO;
        _ipBanDAO = ipBanDAO;
        _loggedUserHolder = loggedUserHolder;
    }

    public boolean resetUserPassword(String login) {
        try {
            final boolean success = _playerDAO.resetUserPassword(login);
            if (!success)
                return false;
            _loggedUserHolder.forceLogoutUser(login);
            return true;
        } catch (SQLException exp) {
            return false;
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

    public void banIp(String login) {
        final User player = _playerDAO.getPlayer(login);
        if (player == null)
            return;
        final String lastIp = player.getLastIp();
        
        _ipBanDAO.addIpBan(lastIp);

        banUser(login);
    }

    public void banIpPrefix(String login) {
        final User player = _playerDAO.getPlayer(login);
        if (player == null)
            return;
        final String lastIp = player.getLastIp();
        String lastIpPrefix = lastIp.substring(0, lastIp.lastIndexOf(".")+1);

        _ipBanDAO.addIpPrefixBan(lastIpPrefix);

        banUser(login);
    }
}
