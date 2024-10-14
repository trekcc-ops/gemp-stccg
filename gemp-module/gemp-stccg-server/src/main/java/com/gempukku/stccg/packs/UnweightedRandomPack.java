package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UnweightedRandomPack extends AbstractPack {

    UnweightedRandomPack(Iterable<String> items) {
        super(items);
    }

    protected String[] parseItem(String item) {
        return item.split("x", 2);
    }

    @Override
    public List<GenericCardItem> openPack() {
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