package com.gempukku.stccg.db;

import java.util.Date;

public interface MerchantDAO {
    Transaction getLastTransaction(String blueprintId);

    void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType);

    enum TransactionType {
        SELL, BUY
    }

    class Transaction {
        private final float _price;
        private final Date _date;
        private final TransactionType _transactionType;

        public Transaction(Date date, float price, TransactionType transactionType) {
            _date = date;
            _price = price;
            _transactionType = transactionType;
        }

        public Date getDate() {
            return _date;
        }

        public float getPrice() {
            return _price;
        }

        public TransactionType getTransactionType() {
            return _transactionType;
        }

    }
}
