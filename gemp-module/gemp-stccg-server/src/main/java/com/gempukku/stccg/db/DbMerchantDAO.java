package com.gempukku.stccg.db;

import java.sql.*;
import java.util.Date;

public class DbMerchantDAO implements MerchantDAO {
    private final DbAccess _dbAccess;

    public DbMerchantDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        final Transaction lastTransaction = getLastTransaction(blueprintId);
        if (lastTransaction == null) {
            insertTransaction(blueprintId, price, date, transactionType);
        } else {
            updateTransaction(blueprintId, price, date, transactionType);
        }
    }

    private void updateTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            String countType = (transactionType == TransactionType.BUY) ? "buy_count" : "sell_count";
            String sqlStatement = "update merchant_data set transaction_price=?, transaction_date=?, " +
                    "transaction_type=?, " + countType + "=" + countType + "+1 where blueprint_id=?";
            SQLUtils.executeUpdateStatementWithParameters(_dbAccess, sqlStatement,
                    price, new Timestamp(date.getTime()), transactionType.name(), blueprintId);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to update last transaction from DB", exp);
        }
    }

    private void insertTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            String sqlStatement =
                    "insert into merchant_data (transaction_price, transaction_date, transaction_type, " +
                    "blueprint_id, sell_count, buy_count) values (?,?,?,?,?,?)";
            SQLUtils.executeStatementWithParameters(_dbAccess, sqlStatement,
                    price, new Timestamp(date.getTime()), transactionType.name(), blueprintId,
                    transactionType == TransactionType.SELL, transactionType == TransactionType.BUY);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to insert last transaction from DB", exp);
        }
    }

    @Override
    public Transaction getLastTransaction(String blueprintId) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                String sqlStatement =
                        "select blueprint_id, transaction_price, transaction_date, transaction_type, " +
                                "buy_count-sell_count from merchant_data where blueprint_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                    statement.setString(1, blueprintId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            float price = rs.getFloat(2);
                            Date date = rs.getTimestamp(3);
                            String type = rs.getString(4);

                            return new Transaction(date, price, TransactionType.valueOf(type));
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get last transaction from DB", exp);
        }
    }
}
