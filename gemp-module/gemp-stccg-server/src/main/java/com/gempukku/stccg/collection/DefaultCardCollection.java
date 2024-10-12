package com.gempukku.stccg.collection;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.common.CardItemType;
import com.gempukku.stccg.packs.ProductLibrary;

import java.util.*;
import java.util.stream.Stream;

public class DefaultCardCollection implements MutableCardCollection {
    private static final String CurrencyKey = "currency";
    private final Map<String, GenericCardItem> _counts = new LinkedHashMap<>();
    private final Map<String, Object> _extraInformation = new HashMap<>();

    public DefaultCardCollection() {
        _extraInformation.put(CurrencyKey,  0);
    }

    public DefaultCardCollection(CardCollection cardCollection) {
        this();
        for (GenericCardItem item : cardCollection.getAll()) {
            _counts.put(item.getBlueprintId(), item);
        }

        _extraInformation.putAll(cardCollection.getExtraInformation());
    }

    public final synchronized void setExtraInformation(Map<String, Object> extraInformation) {
        _extraInformation.putAll(extraInformation);
        //Some deserialization defaults to making the currency a Long rather than an Integer
        if(extraInformation.containsKey(CurrencyKey)) {
            var input = extraInformation.get(CurrencyKey);
            if(input instanceof Long longInput) {
                _extraInformation.put(CurrencyKey, longInput.intValue());
            }
        }
    }

    @Override
    public final synchronized Map<String, Object> getExtraInformation() {
        return Collections.unmodifiableMap(_extraInformation);
    }

    @Override
    public final synchronized void addCurrency(int currency) {
        int oldCurrency = (Integer) _extraInformation.get(CurrencyKey);
        _extraInformation.put(CurrencyKey, oldCurrency + currency);
    }

    @Override
    public final synchronized boolean removeCurrency(int currency) {
        int oldCurrency = (Integer) _extraInformation.get(CurrencyKey);

        if (oldCurrency < currency)
            return false;
        _extraInformation.put(CurrencyKey, oldCurrency - currency);
        return true;
    }

    @Override
    public final synchronized int getCurrency() {
        return (Integer) _extraInformation.get(CurrencyKey);
    }

    @Override
    public final synchronized void addItem(String itemId, int count) {
        if (count > 0) {
            GenericCardItem oldCount = _counts.get(itemId);
            if (oldCount == null) {
                _counts.put(itemId, GenericCardItem.createItem(itemId, count));
            } else
                _counts.put(itemId, GenericCardItem.createItem(itemId, count + oldCount.getCount()));
        }
    }

    @Override
    public final synchronized boolean removeItem(String itemId, int count) {
        if (count > 0) {
            GenericCardItem oldCount = _counts.get(itemId);
            if (oldCount == null || oldCount.getCount() < count)
                return false;
            _counts.put(itemId, GenericCardItem.createItem(itemId, Math.max(0, oldCount.getCount() - count)));
        }
        return true;
    }

    private void addAllItems(GenericCardItem item, DefaultCardCollection coll, ProductLibrary lib) {
        if(item.isRecursive() && item.getType() == CardItemType.PACK) {
            for(int i = 0; i < item.getCount(); i++) {
                var bp = item.getBlueprintId();
                var product = lib.GetProduct(bp);
                if(product == null)
                    continue;
                var children = product.openPack();
                for(var child : children) {
                    addAllItems(child, coll, lib);
                }
            }
        }
        else {
            addItem(item.getBlueprintId(), item.getCount());
            coll.addItem(item.getBlueprintId(), item.getCount());
        }

    }

    @Override
    public final synchronized CardCollection openPack(String packId, String selection, ProductLibrary productLibrary) {
        GenericCardItem count = _counts.get(packId);
        if (count == null)
            return null;
        if (count.getCount() > 0) {
            List<GenericCardItem> packContents = null;
            if (packId.startsWith("(S)")) {
                if (selection != null && hasSelection(packId, selection, productLibrary)) {
                    packContents = new LinkedList<>();
                    packContents.add(GenericCardItem.createItem(selection, 1));
                }
            } else {
                var product = productLibrary.GetProduct(packId);
                if(product == null)
                    return null;
                packContents = product.openPack();
            }

            if (packContents == null)
                return null;

            DefaultCardCollection packCollection = new DefaultCardCollection();

            for (GenericCardItem itemFromPack : packContents) {
                addAllItems(itemFromPack, packCollection, productLibrary);
            }

            removeItem(packId, 1);

            return packCollection;
        }
        return null;
    }

    @Override
    public final synchronized Iterable<GenericCardItem> getAll() {
        return _counts.values();
    }

    @Override
    public final synchronized int getItemCount(String blueprintId) {
        GenericCardItem count = _counts.get(blueprintId);
        if (count == null)
            return 0;
        return count.getCount();
    }

    private static boolean hasSelection(String packId, String selection, ProductLibrary productLibrary) {
        for (GenericCardItem item : productLibrary.GetProduct(packId).openPack()) {
            if (item.getBlueprintId().equals(selection))
                return true;
        }
        return false;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        return this.equals((DefaultCardCollection) obj);
    }

    private final boolean equals(DefaultCardCollection other) {
        if (this == other)
            return true;

        if(!_counts.equals(other._counts))
            return false;

        if(getCurrency() != other.getCurrency())
            return false;

        for(String key : Stream.concat(_counts.keySet().stream(), other._counts.keySet().stream()).distinct().toList()) {
            if(!_counts.containsKey(key) || !other._counts.containsKey(key))
                return false;
            if(!_counts.get(key).equals(other._counts.get(key)))
                return false;
        }

        var keys = Stream.concat(_extraInformation.keySet().stream(),
                        other._extraInformation.keySet().stream())
                .distinct()
                .toList();

        for(String key : keys) {
            if(key.equalsIgnoreCase(CurrencyKey))
                continue;

            if(!_extraInformation.containsKey(key) || !other._extraInformation.containsKey(key))
                return false;
            if(!(_extraInformation.get(key) == null && other._extraInformation.get(key) == null) &&
                    !_extraInformation.get(key).equals(other._extraInformation.get(key)))
                return false;
        }

        return true;
    }
}