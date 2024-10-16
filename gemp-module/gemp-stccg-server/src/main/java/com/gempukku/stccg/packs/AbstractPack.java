package com.gempukku.stccg.packs;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractPack implements PackBox {

    final Map<String, Integer> _contents = new HashMap<>();

    AbstractPack(Iterable<String> items) {
        for (String item : items) {
            String strippedItem = item.strip();
            if (!strippedItem.startsWith("#") && !strippedItem.isEmpty()) {
                String[] result = parseItem(strippedItem);
                _contents.put(result[1], Integer.parseInt(result[0]));
            }
        }
    }

    abstract String[] parseItem(String item);
}