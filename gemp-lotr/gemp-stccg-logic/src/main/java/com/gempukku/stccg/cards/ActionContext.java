package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.EffectResult;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

public interface ActionContext {
    public Map<String, String> getValueMemory();
    public Multimap<String, PhysicalCard> getCardMemory();
    public DefaultGame getGame();
    public void setValueToMemory(String memory, String value);
    public String getValueFromMemory(String memory);
    public void setCardMemory(String memory, PhysicalCard card);
    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards);
    public Collection<PhysicalCard> getCardsFromMemory(String memory);
    public PhysicalCard getCardFromMemory(String memory);
    public String getPerformingPlayer();
    public PhysicalCard getSource();
    public EffectResult getEffectResult();
    public Effect getEffect();
    public Filterable getFilterable(FilterableSource filterableSource);
}
