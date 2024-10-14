package com.gempukku.stccg.collection;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.database.CollectionDAO;
import com.gempukku.stccg.database.DbAccess;
import com.gempukku.stccg.database.DbCollectionDAO;
import com.gempukku.stccg.async.LoggingProxy;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class CachedCollectionDAO implements CollectionDAO, Cached {
    private final CollectionDAO _delegate;
    private final Map<String, CardCollection> _playerCollections = Collections.synchronizedMap(new LRUMap<>(100));

    public CachedCollectionDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(CollectionDAO.class, new DbCollectionDAO(dbAccess));
    }

    @Override
    public void clearCache() {
        _playerCollections.clear();
    }

    @Override
    public int getItemCount() {
        return _playerCollections.size();
    }

    @Override
    public CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException {
        String key = constructCacheKey(playerId, type);
        CardCollection collection = _playerCollections.get(key);
        if (collection == null) {
            collection = _delegate.getPlayerCollection(playerId, type);
            _playerCollections.put(key, collection);
        }
        return collection;
    }

    private String constructCacheKey(int playerId, String type) {
        return playerId +"-"+type;
    }

    @Override
    public Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws SQLException, IOException {
        return _delegate.getPlayerCollectionsByType(type);
    }

    @Override
    public void overwriteCollectionContents(int playerId, String type, CardCollection collection, String reason)
            throws SQLException, IOException {
        _delegate.overwriteCollectionContents(playerId, type, collection, reason);
        recacheCollection(playerId, type);
    }

    @Override
    public void addToCollectionContents(int playerId, String type, CardCollection collection, String source)
            throws SQLException, IOException {
        _delegate.addToCollectionContents(playerId, type, collection, source);
        recacheCollection(playerId, type);
    }

    @Override
    public void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source)
            throws SQLException, IOException {
        _delegate.removeFromCollectionContents(playerId, type, collection, source);
        recacheCollection(playerId, type);
    }

    @Override
    public void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation)
            throws SQLException, IOException {
        _delegate.updateCollectionInfo(playerId, type, extraInformation);
        recacheCollection(playerId, type);
    }

    private void recacheCollection(int playerId, String type) throws SQLException, IOException {
        String id = constructCacheKey(playerId, type);
        CardCollection playerCollection = _delegate.getPlayerCollection(playerId, type);
        _playerCollections.put(id, playerCollection);
    }
}