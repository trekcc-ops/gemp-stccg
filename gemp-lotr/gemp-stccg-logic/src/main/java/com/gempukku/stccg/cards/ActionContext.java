package com.gempukku.stccg.cards;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.EffectResult;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

public interface ActionContext {
    Map<String, String> getValueMemory();
    Multimap<String, PhysicalCard> getCardMemory();
    DefaultGame getGame();
    void setValueToMemory(String memory, String value);
    String getValueFromMemory(String memory);
    void setCardMemory(String memory, PhysicalCard card);
    void setCardMemory(String memory, Collection<? extends PhysicalCard> cards);
    Collection<PhysicalCard> getCardsFromMemory(String memory);
    PhysicalCard getCardFromMemory(String memory);
    String getPerformingPlayer();
    PhysicalCard getSource();
    EffectResult getEffectResult();
    Effect getEffect();
}
