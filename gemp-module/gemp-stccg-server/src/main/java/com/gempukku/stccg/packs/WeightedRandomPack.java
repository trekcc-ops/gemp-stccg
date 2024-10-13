package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.GenericCardItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandomPack implements PackBox {
    private record Reward(String name, int quantity, int weight) { }

    private final Map<String, Reward> _contents = new LinkedHashMap<>();

    public WeightedRandomPack(Iterable<String> items) {
        for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) {
                String[] result = parseItem(item);
                if(result.length != 3) {
                    System.out.println("Unexpected number of entries in a WeightedRandomPack! Skipping: '" +
                            item + "'");
                    continue;
                }
                var reward = new Reward(result[2], Integer.parseInt(result[1]), Integer.parseInt(result[0]));
                _contents.put(reward.name, reward);
            }
        }
    }

    private static String[] parseItem(String item) {
        return item.split("[x%]", 3);
    }

    @Override
    public List<GenericCardItem> openPack() {
        int totalWeight = _contents.values().stream()
                .mapToInt(Reward::weight)
                .sum();

        return openPack(ThreadLocalRandom.current().nextInt(totalWeight) + 1);
    }

    private List<GenericCardItem> openPack(int roll) {
        int currentPercent = 0;

        for (Reward reward : _contents.values()) {
            currentPercent += reward.weight;
            if (roll <= currentPercent) {
                return generateItems(reward.name);
            }
        }

        return generateItems(_contents.keySet().stream().findFirst().orElse(null));
    }

    public List<GenericCardItem> generateItems(String name) {
        var result = GenericCardItem.createItem(name, _contents.get(name).quantity, true);
        return Collections.singletonList(result);
    }

}