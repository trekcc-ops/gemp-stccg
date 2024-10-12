package com.gempukku.stccg.collection;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.db.DbAccess;
import com.gempukku.stccg.db.DbTransferDAO;
import com.gempukku.stccg.async.LoggingProxy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachedTransferDAO implements TransferDAO, Cached {
    private final TransferDAO _delegate;
    private final Set<String> _playersWithoutDelivery = Collections.synchronizedSet(new HashSet<>());

    public CachedTransferDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(TransferDAO.class, new DbTransferDAO(dbAccess));
    }

    @Override
    public void clearCache() {
        _playersWithoutDelivery.clear();
    }

    @Override
    public int getItemCount() {
        return _playersWithoutDelivery.size();
    }

    public boolean hasUndeliveredPackages(String player) {
        if (_playersWithoutDelivery.contains(player))
            return false;
        boolean value = _delegate.hasUndeliveredPackages(player);
        if (!value)
            _playersWithoutDelivery.add(player);
        return value;
    }

    public Map<String, ? extends CardCollection> consumeUndeliveredPackages(String player) {
        return _delegate.consumeUndeliveredPackages(player);
    }

    public void addTransferTo(boolean notifyPlayer, String player, String reason, String collectionName, int currency, CardCollection items) {
        if (notifyPlayer)
            _playersWithoutDelivery.remove(player);
        _delegate.addTransferTo(notifyPlayer, player, reason, collectionName, currency, items);
    }

    public void addTransferFrom(String player, String reason, String collectionName, int currency, CardCollection items) {
        _delegate.addTransferFrom(player, reason, collectionName, currency, items);
    }
}