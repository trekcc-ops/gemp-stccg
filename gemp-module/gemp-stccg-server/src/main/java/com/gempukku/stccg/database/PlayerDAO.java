package com.gempukku.stccg.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface PlayerDAO {
    User getPlayer(int id);
    User getPlayer(String playerName) throws UserNotFoundException;
    boolean resetUserPassword(String login) throws SQLException;

    boolean banPlayerPermanently(String login) throws SQLException;

    boolean banPlayerTemporarily(String login, long dateTo) throws SQLException;

    boolean unBanPlayer(String login) throws SQLException;

    boolean addPlayerFlag(String login, User.Type flag) throws SQLException;
    boolean removePlayerFlag(String login, User.Type flag) throws SQLException;

    List<User> findSimilarAccounts(String login) throws SQLException;
    Set<String> getBannedUsernames() throws SQLException;

    User loginUser(String login, String password);

    void setLastReward(User player, int currentReward) throws SQLException;

    boolean updateLastReward(User player, int previousReward, int currentReward) throws SQLException;

    boolean registerUser(String login, String password, String remoteAddress)
            throws SQLException, LoginInvalidException;

    void updateLastLoginIp(String login, String remoteAddress) throws SQLException;

    List<DBData.DBPlayer> getAllPlayers();

    default String getLastIpForUserName(String userName) throws UserNotFoundException {
        User player = getPlayer(userName);
        return player.getLastIp();
    }
}