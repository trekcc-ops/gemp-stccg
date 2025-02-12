package com.gempukku.stccg.packs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UnweightedRandomPack extends AbstractPack {

    UnweightedRandomPack(@JsonProperty(value = "items", required = true)
            List<String> items,
    @JsonProperty(value = "name", required = true)
                         String name) {
        super(name, items);
    }

    protected String[] parseItem(String item) {
        return item.split("x", 2);
    }

    @Override
    public List<GenericCardItem> openPack(CardBlueprintLibrary library) {
        int selection = ThreadLocalRandom.current().nextInt(_contents.size());
        return openPack(selection);
    }

    private List<GenericCardItem> openPack(int selection) {
        String key = _contents.keySet().stream().skip(selection).findFirst().orElse(null);
        assert key != null;
        var result = GenericCardItem.createItem(key, _contents.get(key), true);
        return Collections.singletonList(result);
    }

}