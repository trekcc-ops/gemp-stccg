package com.gempukku.stccg.packs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FixedPackBox extends AbstractPack {

    FixedPackBox(@JsonProperty(value = "items", required = true)
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
        List<GenericCardItem> result = new LinkedList<>();
        for (Map.Entry<String, Integer> contentsEntry : _contents.entrySet()) {
            String blueprintId = contentsEntry.getKey();
            result.add(GenericCardItem.createItem(blueprintId, contentsEntry.getValue(), false));
        }
        return result;
    }

}