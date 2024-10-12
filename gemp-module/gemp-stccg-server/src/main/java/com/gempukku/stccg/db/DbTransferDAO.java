package com.gempukku.stccg.db;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.MutableCardCollection;
import com.gempukku.stccg.collection.TransferDAO;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbTransferDAO implements TransferDAO {
    private final DbAccess _dbAccess;

    public DbTransferDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public final void addTransferFrom(String player, String reason, String collectionName, int currency,
                                      CardCollection items) {
        addTransfer(false, player, reason, collectionName, currency, items, "from");
    }

    @Override
    public final void addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency,
                                    CardCollection items) {
        addTransfer(notifyPlayer, player, reason, collectionName, currency, items, "to");
    }

    private void addTransfer(boolean notifyPlayer, String player, String reason, String collectionName, int currency,
                              CardCollection items, String direction) {
        if (currency > 0 || items.getAll().iterator().hasNext()) {
            try {
                String sqlStatement = "insert into transfer " +
                        "(notify, player, reason, name, currency, collection, transfer_date, direction) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)";
                SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                        notifyPlayer, player, reason, collectionName, currency, serializeCollection(items),
                        System.currentTimeMillis(), direction);
            } catch (SQLException exp) {
                throw new RuntimeException("Unable to add transfer " + direction, exp);
            }
        }
    }

    @Override
    public final boolean hasUndeliveredPackages(String player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sql = "select count(*) from transfer where player=? and notify=1";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, player);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next())
                            return resultSet.getInt(1) > 0;
                        else
                            return false;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to check if there are any undelivered packages", exp);
        }
    }

    // For now, very naive synchronization
    @Override
    public final synchronized Map<String, ? extends CardCollection> consumeUndeliveredPackages(String player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                Map<String, DefaultCardCollection> result = new HashMap<>();

                String sql = "select name, currency, collection from transfer where player=? and notify=1";

                try (PreparedStatement statement = connection.prepareStatement(sql)){
                    statement.setString(1, player);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            String name = resultSet.getString(1);

                            DefaultCardCollection cardCollection = result.get(name);
                            if (cardCollection == null)
                                cardCollection = new DefaultCardCollection();

                            cardCollection.addCurrency(resultSet.getInt(2));
                            CardCollection retrieved = deserializeCollection(resultSet.getString(3));
                            for (GenericCardItem item : retrieved.getAll())
                                cardCollection.addItem(item.getBlueprintId(), item.getCount());
                            result.put(name, cardCollection);
                        }
                    }
                }

                sql = "update transfer set notify=0 where player=? and notify=1";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, player);
                    statement.executeUpdate();
                }
                return result;
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to consume undelivered packages", exp);
        }
    }

    private static String serializeCollection(CardCollection cardCollection) {
        StringBuilder sb = new StringBuilder();
        for (GenericCardItem item : cardCollection.getAll())
            sb.append(item.getCount()).append("x").append(item.getBlueprintId()).append(",");
        return sb.toString();
    }

    private static CardCollection deserializeCollection(String collection) {
        MutableCardCollection cardCollection = new DefaultCardCollection();
        for (String item : collection.split(",")) {
            if (!item.isEmpty()) {
                String[] itemSplit = item.split("x", 2);
                int count = Integer.parseInt(itemSplit[0]);
                String blueprintId = itemSplit[1];
                cardCollection.addItem(blueprintId, count);
            }
        }

        return cardCollection;
    }
}