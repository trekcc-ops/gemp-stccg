package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.database.CollectionDAO;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.database.UserNotFoundException;
import com.gempukku.stccg.packs.ProductLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionsManager {
    private static final Logger LOGGER = LogManager.getLogger(CollectionsManager.class);
    private final CloseableWriteLock _writeLock;
    private final CloseableReadLock _readLock;
    private final PlayerDAO _playerDAO;
    private final CollectionDAO _collectionDAO;
    private final TransferDAO _transferDAO;

    public CollectionsManager(PlayerDAO playerDAO, CollectionDAO collectionDAO, TransferDAO transferDAO) {
        _playerDAO = playerDAO;
        _collectionDAO = collectionDAO;
        _transferDAO = transferDAO;
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        _writeLock = new CloseableWriteLock(readWriteLock);
        _readLock = new CloseableReadLock(readWriteLock);
    }

    public CardCollection getPlayerMyCardsCollection(User player) {
        try (CloseableReadLock ignored = _readLock.open()) {
            CollectionType type = CollectionType.MY_CARDS;
            String typeString = type.getCode();
            int playerId = player.getId();
            final CardCollection collection = _collectionDAO.getPlayerCollection(playerId, typeString);
            return (collection == null) ? new DefaultCardCollection() : collection;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get player collection", exp);
        }
    }

    public CardCollection getPlayerCollectionWithLibrary(User player, String collectionType,
                                                         CardBlueprintLibrary cardLibrary) {
        try (CloseableReadLock ignored = _readLock.open()) {
            if ("default".equals(collectionType)) {
                return new CompleteCardCollection(cardLibrary);
            }

            if (collectionType.contains("+")) {
                String[] collectionTypes = collectionType.split("\\+");
                List<CardCollection> collections = new ArrayList<>();
                for (String type : collectionTypes) {
                    CardCollection playerCollection = getPlayerCollectionWithLibrary(player, type, cardLibrary);
                    collections.add(playerCollection);
                }
                return new SumCardCollection(collections);
            }

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
        }
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

        try (CloseableWriteLock ignored = _writeLock.open()) {
            overwritePlayerCollection(player, collectionType.getCode(), cardCollection, reason);
            _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(),
                    cardCollection.getCurrency(), cardCollection);
        }
    }

    public Map<User, CardCollection> getPlayersCollection(String collectionType) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        try (CloseableReadLock ignored = _readLock.open()) {
            final Map<Integer, CardCollection> playerCollectionsByType =
                    _collectionDAO.getPlayerCollectionsByType(collectionType);

            Map<User, CardCollection> result = new HashMap<>();
            for (Map.Entry<Integer, CardCollection> playerCollection : playerCollectionsByType.entrySet())
                result.put(_playerDAO.getPlayer(playerCollection.getKey()), playerCollection.getValue());

            return result;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get players collection", exp);
        }
    }

    public CardCollection openPackInPlayerCollection(User player, CollectionType collectionType, String selection,
                                                     ProductLibrary productLibrary, String packId,
                                                     CardBlueprintLibrary cardBlueprintLibrary) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            String collectionTypeCode = collectionType.getCode();
            final CardCollection playerCollection =
                    getPlayerCollectionWithLibrary(player, collectionTypeCode, cardBlueprintLibrary);
            if (playerCollection == null)
                return null;
            MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);

            final CardCollection packContents =
                    mutableCardCollection.openPack(packId, selection, cardBlueprintLibrary, productLibrary);
            if (packContents != null) {
                String reason = "Opened pack";

                CardCollection cardCollection = cardCollectionFromBlueprintId(packId);

                removeFromPlayerCollection(player, collectionTypeCode, cardCollection, reason);
                addToPlayerCollection(player, collectionTypeCode, packContents, reason);

                String playerName = player.getName();
                String collectionTypeName = collectionType.getFullName();
                _transferDAO.addTransferFrom(playerName, reason, collectionTypeName, 0, cardCollection);
                _transferDAO.addTransferTo(true, playerName, reason, collectionTypeName,
                        packContents.getCurrency(), packContents);
            }
            return packContents;
        }
    }

    private CardCollection cardCollectionFromBlueprintId(String blueprintId) {
        MutableCardCollection result = new DefaultCardCollection();
        result.addItem(blueprintId, 1);
        return result;
    }

    public void addItemsToUserCollection(boolean notifyPlayer, String reason, User user, CollectionType collectionType,
                                         List<? extends GenericCardItem> cardItems,
                                         CardBlueprintLibrary cardBlueprintLibrary) {
        CardCollection userCollection = getPlayerCollectionWithLibrary(user, collectionType.getCode(),
                 cardBlueprintLibrary);
        addItemsToUserCollection(notifyPlayer, reason, user, collectionType, cardItems, userCollection);
    }

    private void addItemsToUserCollection(boolean notifyPlayer, String reason, User player,
                                         CollectionType collectionType, Iterable<? extends GenericCardItem> items,
                                         CardCollection playerCollection)  {
        addItemsToUserCollection(notifyPlayer, reason, player, collectionType, items, null,
                playerCollection);
    }

    public void addAllCardsInCollectionToPlayerMyCardsCollection(boolean notifyPlayer, String reason, String player,
                                                  CardCollection collectionItemsToAdd) {
        try {
            User user = _playerDAO.getPlayer(player);
            CardCollection playerCollection = getPlayerMyCardsCollection(user);
            addItemsToUserCollection(notifyPlayer, reason, user, CollectionType.MY_CARDS,
                    collectionItemsToAdd.getAll(), playerCollection);
        } catch(UserNotFoundException exp) {
            LOGGER.error("Unable to find user '" + player + "' to add items to their collection");
        }
    }


    public void addItemsToPlayerMyCardsCollection(boolean notifyPlayer, String reason, String player,
                                                  Iterable<? extends GenericCardItem> items) {
        try {
            User user = _playerDAO.getPlayer(player);
            CardCollection playerCollection = getPlayerMyCardsCollection(user);
            addItemsToUserCollection(notifyPlayer, reason, user, CollectionType.MY_CARDS, items, playerCollection);
        } catch(UserNotFoundException exp) {
            LOGGER.error("Unable to find user '" + player + "' to add items to their collection");
        }
    }


    public void addItemsToUserCollection(boolean notifyPlayer, String reason, User player,
                                         CollectionType collectionType, Iterable<? extends GenericCardItem> items,
                                         Map<String, Object> extraInformation, CardCollection playerCollection){
        try (CloseableWriteLock ignored = _writeLock.open()) {
            String collectionCode = collectionType.getCode();
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
        }
    }


    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, String player, int currency)
            throws SQLException, IOException {
        try {
            addCurrencyToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), currency);
        } catch(UserNotFoundException exp) {
            LOGGER.error("Unable to find player " + player);
        }
    }


    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, User player, int currency)
            throws SQLException, IOException {
        if (currency > 0) {
            try (CloseableWriteLock ignored = _writeLock.open()) {
                CollectionType collectionType = CollectionType.MY_CARDS;
                String collectionTypeCode = collectionType.getCode();
                final CardCollection playerCollection = getPlayerMyCardsCollection(player);
                if (playerCollection != null) {
                    MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                    mutableCardCollection.addCurrency(currency);

                    _collectionDAO.updateCollectionInfo(player.getId(), collectionTypeCode,
                            mutableCardCollection.getExtraInformation());

                    String collectionName = collectionType.getFullName();
                    _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionName, currency,
                            new DefaultCardCollection());
                }
            }
        }
    }

    public boolean removeCurrencyFromPlayerCollection(String reason, User player, int currency)
            throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            CollectionType collectionType = CollectionType.MY_CARDS;
            String collectionTypeCode = collectionType.getCode();
            final CardCollection playerCollection = getPlayerMyCardsCollection(player);
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
        }
    }

    public void setLastReward(User user, int latestMonday) throws SQLException {
        _playerDAO.setLastReward(user, latestMonday);
    }

    public boolean updateLastReward(User user, Integer lastReward, int latestMonday) throws SQLException {
       return _playerDAO.updateLastReward(user, lastReward, latestMonday);
    }
}