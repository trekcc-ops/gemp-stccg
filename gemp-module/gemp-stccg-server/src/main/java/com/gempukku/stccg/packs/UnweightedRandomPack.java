package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class UnweightedRandomPack implements PackBox {
    private final Map<String, Integer> _contents = new LinkedHashMap<>();

    private UnweightedRandomPack() {
    }

    public static UnweightedRandomPack LoadFromArray(Iterable<String> items) {
        UnweightedRandomPack box = new UnweightedRandomPack();
        for (String item : items) {
            String trimmedItem = item.strip();
            if (!trimmedItem.startsWith("#") && !trimmedItem.isEmpty()) {
                String[] result = trimmedItem.split("x", 2);
                box._contents.put(result[1], Integer.parseInt(result[0]));
            }
        }

        return box;
    }

    @Override
    public final List<GenericCardItem> openPack() {
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