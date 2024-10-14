package com.gempukku.stccg.database;

import com.gempukku.stccg.collection.CardCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public interface CollectionDAO {
    Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws SQLException, IOException;

    CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException;

    void overwriteCollectionContents(int playerId, String type, CardCollection collection, String reason)
            throws SQLException, IOException;

    void addToCollectionContents(int playerId, String type, CardCollection collection, String source)
            throws SQLException, IOException;

    void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source)
            throws SQLException, IOException;

    void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation)
            throws SQLException, IOException;
}