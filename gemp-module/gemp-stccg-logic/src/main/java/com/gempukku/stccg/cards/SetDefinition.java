package com.gempukku.stccg.cards;

import java.util.*;

public class SetDefinition {
    private final Map<String, List<String>> _rarityList = new HashMap<>();
    private final Map<String, String> _cardsRarity = new LinkedHashMap<>();
    private final String _setId;
    private final Set<String> _flags;
    private final String _setName;

    public SetDefinition(String setId, String setName, Set<String> flags) {
        _setId = setId;
        _flags = flags;
        _setName = setName;
    }

    public void addCard(String blueprintId, String rarity) {
        _cardsRarity.put(blueprintId, rarity);
        List<String> cardsOfRarity = _rarityList.computeIfAbsent(rarity, k -> new LinkedList<>());
        cardsOfRarity.add(blueprintId);
    }

    public String getSetId() {
        return _setId;
    }

    public String getSetName() { return _setName; }

    public boolean hasFlag(String flag) {
        return _flags.contains(flag);
    }

    public List<String> getCardsOfRarity(String rarity) {
        final List<String> list = _rarityList.get(rarity);
        if (list == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    public String getCardRarity(String cardId) {
        return _cardsRarity.get(cardId);
    }

    public Set<String> getAllCards() {
        return Collections.unmodifiableSet(_cardsRarity.keySet());
    }
}