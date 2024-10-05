package com.gempukku.stccg.db;

import com.gempukku.stccg.cache.Cached;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class CachedMerchantDAO implements MerchantDAO, Cached {
    private final MerchantDAO _delegate;
    private final Map<String, Transaction> _blueprintIdLastTransaction = Collections.synchronizedMap(new LRUMap<>(4000));
    private final Transaction _nullTransaction = new Transaction(null, 0, null);

    public CachedMerchantDAO(MerchantDAO delegate) {
        _delegate = delegate;
    }

    @Override
    public void clearCache() {
        _blueprintIdLastTransaction.clear();
    }

    @Override
    public int getItemCount() {
        return _blueprintIdLastTransaction.size();
    }

    @Override
    public void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        _delegate.addTransaction(blueprintId, price, date, transactionType);
        _blueprintIdLastTransaction.remove(blueprintId);
    }

    @Override
    public Transaction getLastTransaction(String blueprintId) {
        Transaction transaction = _blueprintIdLastTransaction.get(blueprintId);
        if (transaction == null) {
            transaction = _delegate.getLastTransaction(blueprintId);
            _blueprintIdLastTransaction.put(blueprintId, Objects.requireNonNullElse(transaction, _nullTransaction));
        } else if (transaction == _nullTransaction) {
            transaction = null;
        }
        return transaction;
    }
}
