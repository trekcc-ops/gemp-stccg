package com.gempukku.stccg.merchant;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.cards.SetDefinition;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("NestedMethodCall")
public class MerchantService {
    private final Map<String, PriceGuarantee> _priceGuarantees = Collections.synchronizedMap(new LRUMap<>(100));

    private final ReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final Set<BasicCardItem> _merchantableItems = new HashSet<>();

    private final CollectionType _permanentCollection = CollectionType.MY_CARDS;
    private final CollectionsManager _collectionsManager;

    public MerchantService(CardBlueprintLibrary library, CollectionsManager collectionsManager) {
        _collectionsManager = collectionsManager;

        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.hasFlag("merchantable")) {
                for (String blueprintId : setDefinition.getAllCards()) {
                    String baseBlueprintId = library.getBaseBlueprintId(blueprintId);
                    _merchantableItems.add(new BasicCardItem(baseBlueprintId));
                }
            }
        }
    }

    public Set<BasicCardItem> getSellableItems() {
        return Collections.unmodifiableSet(_merchantableItems);
    }

    public PriceGuarantee priceCards(User player) {
        Lock lock = _lock.readLock();
        lock.lock();
        try {
            Map<String, Integer> buyPrices = new HashMap<>();
            Map<String, Integer> sellPrices = new HashMap<>();
            PriceGuarantee priceGuarantee = new PriceGuarantee(sellPrices, buyPrices);
            _priceGuarantees.put(player.getName(), priceGuarantee);
            return priceGuarantee;
        } finally {
            lock.unlock();
        }
    }

    public void merchantBuysCard(User player, String blueprintId, int price) throws MerchantException, SQLException, IOException {
        priceCards(player);

        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            PriceGuarantee guarantee = _priceGuarantees.get(player.getName());
            guarantee.getBuyPrices().get(blueprintId);

            boolean success = _collectionsManager.sellCardInPlayerCollection(player, _permanentCollection, blueprintId, price);
            if (!success)
                throw new MerchantException("Unable to remove the sold card from your collection");

            _priceGuarantees.remove(player.getName());
        } finally {
            lock.unlock();
        }
    }

    public void merchantSellsCard(User player, String blueprintId, int price) throws MerchantException, SQLException, IOException {
        priceCards(player);

        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            PriceGuarantee guarantee = _priceGuarantees.get(player.getName());
            guarantee.getSellPrices().get(blueprintId);

            boolean success = _collectionsManager.buyCardToPlayerCollection(player, _permanentCollection, blueprintId, price);
            if (!success)
                throw new MerchantException("Unable to remove required currency from your collection");

            _priceGuarantees.remove(player.getName());
        } finally {
            lock.unlock();
        }
    }

    public void tradeForFoil(User player, String blueprintId) throws MerchantException, SQLException, IOException {
        if (!blueprintId.contains("_") || blueprintId.endsWith("*"))
            throw new MerchantException("Unable to trade in this type of item");
        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            boolean success = _collectionsManager.tradeCards(player, _permanentCollection, blueprintId, 4, blueprintId + "*", 1, 400);
            if (!success)
                throw new MerchantException("Unable to remove the required cards or currency from your collection");
        } finally {
            lock.unlock();
        }
    }

    public static class PriceGuarantee {
        private final Map<String, Integer> _sellPrices;
        private final Map<String, Integer> _buyPrices;

        private PriceGuarantee(Map<String, Integer> sellPrices, Map<String, Integer> buyPrices) {
            _sellPrices = sellPrices;
            _buyPrices = buyPrices;
        }

        public Map<String, Integer> getBuyPrices() {
            return _buyPrices;
        }

        public Map<String, Integer> getSellPrices() {
            return _sellPrices;
        }
    }
}