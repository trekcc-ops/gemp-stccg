package com.gempukku.stccg.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DbIpBanDAO implements IpBanDAO {
    private static final String GET_IP_BANS_STATEMENT = "select ip from ip_ban where prefix=0";
    private static final String GET_IP_PREFIX_BANS_STATEMENT = "select ip from ip_ban where prefix=1";
    private final DbAccess _dbAccess;

    public DbIpBanDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final void addIpBan(String ip) {
        try {
            String sqlStatement = "insert into ip_ban (ip, prefix) values (?, 0)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement, ip);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to add an IP ban", exp);
        }
    }

    @Override
    public final void addIpPrefixBan(String ipPrefix) {
        try {
            String sqlStatement = "insert into ip_ban (ip, prefix) values (?, 1)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement, ipPrefix);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to add an IP prefix ban", exp);
        }
    }

    @Override
    public final Set<String> getIpBans() {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(GET_IP_BANS_STATEMENT)) {
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            String ip = rs.getString(1);

                            result.add(ip);
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of player games", exp);
        }
    }

    @Override
    public final Set<String> getIpPrefixBans() {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(GET_IP_PREFIX_BANS_STATEMENT)) {
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            String ip = rs.getString(1);

                            result.add(ip);
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get count of player games", exp);
        }
    }

    public boolean isIpBanned(String ip) {
        if (getIpBans().contains(ip))
            return true;
        for (String bannedRange : getIpPrefixBans()) {
            if (ip.startsWith(bannedRange))
                return true;
        }
        return false;
    }

}