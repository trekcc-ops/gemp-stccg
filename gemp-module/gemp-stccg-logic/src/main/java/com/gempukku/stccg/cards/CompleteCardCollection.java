package com.gempukku.stccg.cards;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CompleteCardCollection implements CardCollection {

    private final CardBlueprintLibrary _library;
    private final int _completeCount = 4;

    public CompleteCardCollection(CardBlueprintLibrary library) {
        _library = library;
    }
    @Override
    public int getCurrency() {
        return 0;
    }

    @Override
    public Iterable<Item> getAll() {
        return _library.getBaseCards().keySet().stream().map(blueprintId ->
                Item.createItem(blueprintId, _completeCount)).collect(Collectors.toList());
    }

    @Override
    public int getItemCount(String blueprintId) {
        return _library.getBaseBlueprintId(blueprintId).equals(blueprintId) ? _completeCount : 0;
    }

    @Override
    public Map<String, Object> getExtraInformation() {
        return Collections.emptyMap();
    }
}
