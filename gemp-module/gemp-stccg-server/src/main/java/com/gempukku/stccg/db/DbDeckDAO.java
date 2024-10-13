package com.gempukku.stccg.db;

import com.gempukku.stccg.common.CardDeck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbDeckDAO implements DeckDAO {
    private final DbAccess _dbAccess;

    public DbDeckDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public final synchronized CardDeck getDeckForPlayer(User player, String name) {
        return getPlayerDeck(player.getId(), name);
    }

    public final synchronized void saveDeckForPlayer(User player, String name, String targetFormat, String notes,
                                                     CardDeck deck) {
        boolean newDeck = getPlayerDeck(player.getId(), name) == null;
        storeDeckToDB(player.getId(), name, targetFormat, notes, deck, newDeck);
    }

    public final synchronized void deleteDeckForPlayer(User player, String name) {
        try {
            String sqlStatement = "delete from deck where player_id=? and name=?";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    player.getId(), name);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to delete player deck from DB", exp);
        }
    }

    public final synchronized CardDeck renameDeck(User player, String oldName, String newName) {
        CardDeck deck = getDeckForPlayer(player, oldName);
        if (deck == null)
            return null;
        saveDeckForPlayer(player, newName, deck.getTargetFormat(), deck.getNotes(), deck);
        deleteDeckForPlayer(player, oldName);

        return deck;
    }

    public final synchronized Set<Map.Entry<String, String>> getPlayerDeckNames(User player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlStatement = "select name, target_format from deck where player_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                    statement.setInt(1, player.getId());
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<Map.Entry<String, String>> result = new HashSet<>();

                        while (rs.next()) {
                            String deckName = rs.getString(1);
                            String targetFormat = rs.getString(2);
                            result.add(new AbstractMap.SimpleEntry<>(targetFormat, deckName));
                        }

                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to load player decks from DB", exp);
        }
    }

    private CardDeck getPlayerDeck(int playerId, String name) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlStatement = "select contents, target_format, notes from deck where player_id=? and name=?";
                try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                    statement.setInt(1, playerId);
                    statement.setString(2, name);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next())
                            return new CardDeck(name, rs.getString(1), rs.getString(2),
                                    rs.getString(3));

                        return null;
                    }
                }
            }

        } catch (SQLException exp) {
            throw new RuntimeException("Unable to load player decks from DB", exp);
        }
    }

    private void storeDeckToDB(int playerId, String name, String targetFormat, String notes, CardDeck deck,
                               boolean newDeck) {
        String contents = deck.buildContentsFromDeck();
        try {
            if (newDeck) {
                String sqlStatement =
                        "insert into deck (player_id, name, target_format, notes, contents) values (?, ?, ?, ?, ?)";
                SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                        playerId, name, targetFormat, notes, contents);
            }
            else {
                String sqlStatement =
                        "update deck set contents=?, target_format=?, notes=? where player_id=? and name=?";
                SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                        contents, targetFormat, notes, playerId, name);
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to store player deck to DB", exp);
        }
    }

}