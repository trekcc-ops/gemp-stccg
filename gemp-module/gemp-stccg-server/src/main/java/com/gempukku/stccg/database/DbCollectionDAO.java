package com.gempukku.stccg.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbCollectionDAO implements CollectionDAO {
    private final DbAccess _dbAccess;
    private final ObjectMapper _mapper = new ObjectMapper();

    public DbCollectionDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public final Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws IOException {
        Map<Integer, CardCollection> result = new HashMap<>();
        for(var coll : getCollectionInfoByType(type))
            result.put(coll.player_id, deserializeCollection(coll, extractCollectionEntries(coll.id)));
        return result;
    }

    public final CardCollection getPlayerCollection(int playerId, String type) throws IOException {
        DBData.Collection collection = getCollectionInfo(playerId, type);
        return (collection == null) ? null : deserializeCollection(collection, extractCollectionEntries(collection.id));
    }

    @SuppressWarnings("TrailingWhitespacesInTextBlock")
    private List<DBData.CollectionEntry> extractCollectionEntries(int collectionID) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            collection_id, 
                            quantity, 
                            product_type, 
                            product_variant, 
                            product, 
                            source, 
                            created_date, 
                            modified_date, 
                            notes
                        FROM gemp_db.collection_entries
                        WHERE collection_id = :collID;
                        """;

                return conn.createQuery(sql)
                        .addParameter("collID", collectionID)
                        .executeAndFetch(DBData.CollectionEntry.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection entries", ex);
        }
    }

    private DBData.Collection getCollectionInfo(int playerId, String type) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type
                            AND player_id = :playerID
                        LIMIT 1;
                        """;
                List<DBData.Collection> result = conn.createQuery(sql)
                        .addParameter("type", type)
                        .addParameter("playerID", playerId)
                        .executeAndFetch(DBData.Collection.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection info", ex);
        }
    }

    private List<DBData.Collection> getCollectionInfoByType(String type) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type;
                        """;

                return conn.createQuery(sql)
                        .addParameter("type", type)
                        .executeAndFetch(DBData.Collection.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection database definitions", ex);
        }
    }

    private final int getCollectionID(int playerId, String type)  {
        var coll = getCollectionInfo(playerId,  type);
        if(coll == null)
            return -1;

        return coll.id;
    }


    @SuppressWarnings("SpellCheckingInspection")
    public final void overwriteCollectionContents(int playerId, String type, CardCollection collection, String reason) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = :quantity, source = :source;
                        """;
        String error = "Unable to update product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, reason, sql, error);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public final void addToCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = quantity + :quantity, source = :source;
                        """;
        String error = "Unable to add product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public final void removeFromCollectionContents(int playerId, String type, CardCollection collection,
                                                   String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = GREATEST(quantity - :quantity, 0), source = :source;
                        """;
        String error = "Unable to remove product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    @Override
    public final void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        upsertCollectionInfo(playerId, type, extraInformation);
    }


    @SuppressWarnings("SpellCheckingInspection")
    private void updateCollectionContents(int playerId, String type, CardCollection collection, String source,
                                          String sql, String error) {
        if(getCollectionID(playerId, type) <= 0) {
            upsertCollectionInfo(playerId, type, collection.getExtraInformation());
        }
        int collID = getCollectionID(playerId, type);

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                for(var card : collection.getAll()) {
                    query.addParameter("collid", collID)
                            .addParameter("quantity", card.getCount())
                            .addParameter("type", card.getType())
                            .addParameter("product", card.getBlueprintId())
                            .addParameter("source", source)
                            .addToBatch();
                }
                query.executeBatch();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException(error, ex);
        }
    }

    private void upsertCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        String sql = """
                        INSERT INTO collection(player_id, type, extra_info)
                        VALUES (:playerId, :type, :extraInfo)
                        ON DUPLICATE KEY UPDATE extra_info = :extraInfo;
                        """;
        String json;
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.valueToTree(extraInformation));

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("playerId", playerId)
                        .addParameter("type", type)
                        .addParameter("extraInfo", json);
                query.executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to upsert collection", ex);
        }
    }

    private MutableCardCollection deserializeCollection(DBData.Collection coll,
                                                        Iterable<? extends DBData.CollectionEntry> entries)
            throws IOException {
        MutableCardCollection newColl = new DefaultCardCollection();

        if(coll.extra_info != null) {
            newColl.setExtraInformation(_mapper.convertValue(
                    _mapper.readTree(coll.extra_info), new TypeReference<>() {}));
        }

        for(var entry : entries) {
            newColl.addItem(entry.product, entry.quantity);
        }

        return newColl;
    }

}