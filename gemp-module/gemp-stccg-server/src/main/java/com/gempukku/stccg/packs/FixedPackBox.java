package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FixedPackBox implements PackBox {
    private final Map<String, Integer> _contents = new LinkedHashMap<>();

    public static FixedPackBox LoadFromArray(Iterable<String> items) {
        FixedPackBox box = new FixedPackBox();
        for (String item : items) {
            item = item.trim();
            if (!item.startsWith("#") && !item.isEmpty()) {
                String[] result = item.split("x", 2);
                box._contents.put(result[1], Integer.parseInt(result[0]));
            }
        }

        return box;
    }

    @Override
    public List<GenericCardItem> openPack() {
        List<GenericCardItem> result = new LinkedList<>();
        for (Map.Entry<String, Integer> contentsEntry : _contents.entrySet()) {
            String blueprintId = contentsEntry.getKey();
            result.add(GenericCardItem.createItem(blueprintId, contentsEntry.getValue(), false));
        }
        return result;
    }

}