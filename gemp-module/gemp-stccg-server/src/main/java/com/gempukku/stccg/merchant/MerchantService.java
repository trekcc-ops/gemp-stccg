package com.gempukku.stccg.merchant;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.SetDefinition;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings({"NestedMethodCall", "LongLine"})
public class MerchantService {
    private final Map<String, PriceGuarantee> _priceGuarantees = Collections.synchronizedMap(new LRUMap<>(100));

    private final ReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final Set<BasicCardItem> _merchantableItems = new HashSet<>();

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