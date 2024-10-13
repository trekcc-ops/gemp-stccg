package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.database.CollectionDAO;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.packs.ProductLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionsManager {
    private final ReadWriteLock _readWriteLock = new ReentrantReadWriteLock();
    private final PlayerDAO _playerDAO;
    private final CollectionDAO _collectionDAO;
    private final TransferDAO _transferDAO;
    private final CardBlueprintLibrary _cardLibrary;

    private CardCollection _defaultCollection;

    public CollectionsManager(PlayerDAO playerDAO, CollectionDAO collectionDAO, TransferDAO transferDAO,
                              final CardBlueprintLibrary cardBlueprintLibrary) {
        _playerDAO = playerDAO;
        _collectionDAO = collectionDAO;
        _transferDAO = transferDAO;
        _cardLibrary = cardBlueprintLibrary;

        _defaultCollection = new CompleteCardCollection(_cardLibrary);

        _cardLibrary.SubscribeToRefreshes(() -> _defaultCollection = new CompleteCardCollection(_cardLibrary));
    }

    public CardCollection getCompleteCardCollection() {
        return _defaultCollection;
    }

    public CardCollection getPlayerCollection(User player, String collectionType) {
        Lock lock = _readWriteLock.readLock();
        lock.lock();
        try {
            if (collectionType.contains("+")) {
                String[] collectionTypes = collectionType.split("\\+");
                return createSumCollection(player, collectionTypes);
            }

            if ("default".equals(collectionType))
                return _defaultCollection;

            int playerId = player.getId();
            final CardCollection collection = _collectionDAO.getPlayerCollection(playerId, collectionType);
            String trophyCode = CollectionType.TROPHY.getCode();
            String myCardsCode = CollectionType.MY_CARDS.getCode();
            if (collection == null && (collectionType.equals(myCardsCode) ||
                    collectionType.equals(trophyCode)))
                return new DefaultCardCollection();

            return collection;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get player collection", exp);
        } finally {
            lock.unlock();
        }
    }

    private CardCollection createSumCollection(User player, String[] collectionTypes) {
        List<CardCollection> collections = new LinkedList<>();
        for (String collectionType : collectionTypes) {
            CardCollection playerCollection = getPlayerCollection(player, collectionType);
            collections.add(playerCollection);
        }

        return new SumCardCollection(collections);
    }

    private void overwritePlayerCollection(User player, String collectionType, CardCollection cardCollection,
                                           String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.overwriteCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    private void addToPlayerCollection(User player, String collectionType, CardCollection cardCollection,
                                       String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.addToCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    private void removeFromPlayerCollection(User player, String collectionType, CardCollection cardCollection,
                                            String reason) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.removeFromCollectionContents(player.getId(), collectionType, cardCollection, reason);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    public void addPlayerCollection(boolean notifyPlayer, String reason, User player, CollectionType collectionType,
                                    CardCollection cardCollection) {
        if (collectionType.getCode().contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.writeLock().lock();
        try {
            overwritePlayerCollection(player, collectionType.getCode(), cardCollection, reason);
            _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(),
                    cardCollection.getCurrency(), cardCollection);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public Map<User, CardCollection> getPlayersCollection(String collectionType) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.readLock().lock();
        try {
            final Map<Integer, CardCollection> playerCollectionsByType =
                    _collectionDAO.getPlayerCollectionsByType(collectionType);

            Map<User, CardCollection> result = new HashMap<>();
            for (Map.Entry<Integer, CardCollection> playerCollection : playerCollectionsByType.entrySet())
                result.put(_playerDAO.getPlayer(playerCollection.getKey()), playerCollection.getValue());

            return result;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get players collection", exp);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }

    public CardCollection openPackInPlayerCollection(User player, CollectionType collectionType, String selection,
                                                     ProductLibrary productLibrary, String packId) {
        Lock lock = _readWriteLock.writeLock();
        lock.lock();
        try {
            String collectionTypeCode = collectionType.getCode();
            final CardCollection playerCollection = getPlayerCollection(player, collectionTypeCode);
            if (playerCollection == null)
                return null;
            MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);

            final CardCollection packContents = mutableCardCollection.openPack(packId, selection, productLibrary);
            if (packContents != null) {
                String reason = "Opened pack";

                CardCollection cardCollection = cardCollectionFromBlueprintId(1, packId);

                removeFromPlayerCollection(player, collectionTypeCode, cardCollection, reason);
                addToPlayerCollection(player, collectionTypeCode, packContents, reason);

                String playerName = player.getName();
                String collectionTypeName = collectionType.getFullName();
                _transferDAO.addTransferFrom(playerName, reason, collectionTypeName, 0, cardCollection);
                _transferDAO.addTransferTo(true, playerName, reason, collectionTypeName,
                        packContents.getCurrency(), packContents);
            }
            return packContents;
        } finally {
            lock.unlock();
        }
    }

    private CardCollection cardCollectionFromBlueprintId(int count, String blueprintId) {
        MutableCardCollection result = new DefaultCardCollection();
        result.addItem(blueprintId, count);
        return result;
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, User player,
                                           CollectionType collectionType, Iterable<? extends GenericCardItem> items,
                                           Map<String, Object> extraInformation){
        _readWriteLock.writeLock().lock();
        try {
            String collectionCode = collectionType.getCode();
            final CardCollection playerCollection = getPlayerCollection(player, collectionCode);
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                MutableCardCollection addedCards = new DefaultCardCollection();
                for (GenericCardItem item : items) {
                    mutableCardCollection.addItem(item.getBlueprintId(), item.getCount());
                    addedCards.addItem(item.getBlueprintId(), item.getCount());
                }

                if (extraInformation != null) {
                    Map<String, Object> resultExtraInformation = new HashMap<>(playerCollection.getExtraInformation());
                    resultExtraInformation.putAll(extraInformation);
                    _collectionDAO.updateCollectionInfo(player.getId(), collectionCode, resultExtraInformation);
                }

                addToPlayerCollection(player, collectionCode, addedCards, reason);

                String playerName = player.getName();
                String collectionName = collectionType.getFullName();
                _transferDAO.addTransferTo(notifyPlayer, playerName, reason, collectionName, 0, addedCards);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Could not add items to player collection", e);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, User player,
                                           CollectionType collectionType, Iterable<? extends GenericCardItem> items)  {
        addItemsToPlayerCollection(notifyPlayer, reason, player, collectionType, items, null);
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, String player,
                                           CollectionType collectionType, Iterable<? extends GenericCardItem> items)  {
        addItemsToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, items);
    }

    public boolean tradeCards(User player, CollectionType collectionType, String removeBlueprintId, int removeCount,
                              String addBlueprintId, int addCount, int currencyCost) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            String collectionTypeCode = collectionType.getCode();
            String collectionFullName = collectionType.getFullName();
            String playerName = player.getName();

            final CardCollection playerCollection = getPlayerCollection(player, collectionTypeCode);
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(removeBlueprintId, removeCount))
                    return false;
                if (!mutableCardCollection.removeCurrency(currencyCost))
                    return false;
                mutableCardCollection.addItem(addBlueprintId, addCount);

                String reason = "Trading items";

                CardCollection removedCollection = cardCollectionFromBlueprintId(removeCount, removeBlueprintId);
                CardCollection addedCollection = cardCollectionFromBlueprintId(addCount, addBlueprintId);

                removeFromPlayerCollection(player, collectionTypeCode, removedCollection, reason);
                addToPlayerCollection(player, collectionTypeCode, addedCollection, reason);
                removeCurrencyFromPlayerCollection(reason, player, collectionType, currencyCost);

                _transferDAO.addTransferFrom(playerName, reason, collectionFullName, currencyCost, removedCollection);
                _transferDAO.addTransferTo(
                        true, playerName, reason, collectionFullName, 0, addedCollection);

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean buyCardToPlayerCollection(User player, CollectionType collectionType, String blueprintId,
                                             int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            String collectionCode = collectionType.getCode();
            final CardCollection playerCollection = getPlayerCollection(player, collectionCode);
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeCurrency(currency))
                    return false;
                mutableCardCollection.addItem(blueprintId, 1);

                String reason = "Items bought";

                removeCurrencyFromPlayerCollection(reason, player, collectionType, currency);
                CardCollection collection = cardCollectionFromBlueprintId(1, blueprintId);
                addToPlayerCollection(player, collectionCode, collection, reason);

                String playerName = player.getName();
                String collectionName = collectionType.getFullName();
                _transferDAO.addTransferFrom(playerName, reason, collectionName, currency, new DefaultCardCollection());
                _transferDAO.addTransferTo(true, playerName, reason, collectionName, 0, collection);

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean sellCardInPlayerCollection(User player, CollectionType collectionType, String blueprintId,
                                              int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(blueprintId, 1))
                    return false;

                String reason = "Selling items";

                addCurrencyToPlayerCollection(false, reason, player, collectionType, currency);

                CardCollection collection = cardCollectionFromBlueprintId(1, blueprintId);
                removeFromPlayerCollection(player, collectionType.getCode(), collection, reason);

                String collectionName = collectionType.getFullName();
                _transferDAO.addTransferFrom(player.getName(), reason, collectionName, 0, collection);
                _transferDAO.addTransferTo(false, player.getName(), reason, collectionName, currency,
                        new DefaultCardCollection());

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, String player,
                                              CollectionType collectionType, int currency)
            throws SQLException, IOException {
        addCurrencyToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, currency);
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, User player,
                                              CollectionType collectionType, int currency)
            throws SQLException, IOException {
        if (currency > 0) {
            _readWriteLock.writeLock().lock();
            try {
                String collectionTypeCode = collectionType.getCode();
                final CardCollection playerCollection = getPlayerCollection(player, collectionTypeCode);
                if (playerCollection != null) {
                    MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                    mutableCardCollection.addCurrency(currency);

                    _collectionDAO.updateCollectionInfo(player.getId(), collectionTypeCode,
                            mutableCardCollection.getExtraInformation());

                    String collectionName = collectionType.getFullName();
                    _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionName, currency,
                            new DefaultCardCollection());
                }
            } finally {
                _readWriteLock.writeLock().unlock();
            }
        }
    }

    public boolean removeCurrencyFromPlayerCollection(String reason, User player, CollectionType collectionType,
                                                      int currency) throws SQLException, IOException {
        _readWriteLock.writeLock().lock();
        try {
            String collectionTypeCode = collectionType.getCode();
            final CardCollection playerCollection = getPlayerCollection(player, collectionTypeCode);
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (mutableCardCollection.removeCurrency(currency)) {
                    _collectionDAO.updateCollectionInfo(player.getId(), collectionTypeCode,
                            mutableCardCollection.getExtraInformation());

                    String collectionName = collectionType.getFullName();
                    String playerName = player.getName();
                    _transferDAO.addTransferFrom(playerName, reason, collectionName, currency,
                            new DefaultCardCollection());

                    return true;
                }
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }
}