package com.gempukku.stccg.cards;

import java.util.*;

public class DefaultSetDefinition implements SetDefinition {
    private final List<String> _tengwarCards = new LinkedList<>();
    private final Map<String, List<String>> _rarityList = new HashMap<>();
    private final Map<String, String> _cardsRarity = new LinkedHashMap<>();
    private final String _setId;
    private final Set<String> _flags;

    public DefaultSetDefinition(String setId, Set<String> flags) {
        _setId = setId;
        _flags = flags;
    }

    public void addCard(String blueprintId, String rarity) {
        _cardsRarity.put(blueprintId, rarity);
        List<String> cardsOfRarity = _rarityList.computeIfAbsent(rarity, k -> new LinkedList<>());
        cardsOfRarity.add(blueprintId);
    }

    @Override
    public String getSetId() {
        return _setId;
    }

    @Override
    public boolean hasFlag(String flag) {
        return _flags.contains(flag);
    }

    @Override
    public List<String> getCardsOfRarity(String rarity) {
        final List<String> list = _rarityList.get(rarity);
        if (list == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<String> getTengwarCards() {
        return Collections.unmodifiableList(_tengwarCards);
    }

    @Override
    public String getCardRarity(String cardId) {
        return _cardsRarity.get(cardId);
    }

    @Override
    public Set<String> getAllCards() {
        return Collections.unmodifiableSet(_cardsRarity.keySet());
    }
}
