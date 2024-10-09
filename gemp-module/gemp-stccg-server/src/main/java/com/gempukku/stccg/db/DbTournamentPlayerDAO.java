package com.gempukku.stccg.db;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.tournament.TournamentPlayerDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbTournamentPlayerDAO implements TournamentPlayerDAO {
    private final DbAccess _dbAccess;

    public DbTournamentPlayerDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addPlayer(String tournamentId, String playerName, CardDeck deck) {
        try {
            String sqlMessage =
                    "insert into tournament_player (tournament_id, player, deck_name, deck) values (?, ?, ?, ?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlMessage,
                    tournamentId, playerName, deck.getDeckName(), deck.buildContentsFromDeck());
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void updatePlayerDeck(String tournamentId, String playerName, CardDeck deck) {
        try {
            String sqlMessage =
                    "update tournament_player set deck_name = ?, deck = ? where tournament_id=? and player=?";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlMessage,
                    deck.getDeckName(), deck.buildContentsFromDeck(), tournamentId, playerName);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public void dropPlayer(String tournamentId, String playerName) {
        try {
            String sqlMessage = "update tournament_player set dropped=true where tournament_id=? and player=?";
            SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlMessage,
                    tournamentId, playerName);
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Set<String> getPlayers(String tournamentId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select player from tournament_player where tournament_id=?")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            String player = rs.getString(1);
                            result.add(player);
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Map<String, CardDeck> getPlayerDecks(String tournamentId, String format) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select player, deck_name, deck from tournament_player where tournament_id=? and deck_name is not null")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Map<String, CardDeck> result = new HashMap<>();
                        while (rs.next()) {
                            String player = rs.getString(1);
                            String deckName = rs.getString(2);
                            String contents = rs.getString(3);

                            result.put(player, new CardDeck(deckName, contents, format, ""));
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public Set<String> getDroppedPlayers(String tournamentId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select player from tournament_player where tournament_id=? and dropped=true")) {
                    statement.setString(1, tournamentId);
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<>();
                        while (rs.next()) {
                            result.add(rs.getString(1));
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }

    @Override
    public CardDeck getPlayerDeck(String tournamentId, String playerName, String format) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select deck_name, deck from tournament_player where tournament_id=? and player=?")) {
                    statement.setString(1, tournamentId);
                    statement.setString(2, playerName);
                    try (ResultSet rs = statement.executeQuery()) {
                        return (rs.next()) ?
                                new CardDeck(rs.getString(1), rs.getString(2), format, "") :
                                null;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException(exp);
        }
    }
}
