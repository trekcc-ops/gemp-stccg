package com.gempukku.stccg.cards;

import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.EffectResult;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericActionContext implements ActionContext {
    protected final String performingPlayer;
    protected final PhysicalCard source;
    protected final EffectResult effectResult;
    protected final Effect effect;
    protected final Multimap<String, PhysicalCard> _cardMemory = HashMultimap.create();
    protected final Map<String, String> _valueMemory = new HashMap<>();
    protected GenericActionContext(String performingPlayer, PhysicalCard source, EffectResult effectResult, Effect effect) {
        this.performingPlayer = performingPlayer;
        this.source = source;
        this.effectResult = effectResult;
        this.effect = effect;
    }
    public Map<String, String> getValueMemory() { return _valueMemory; }
    public Multimap<String, PhysicalCard> getCardMemory() { return _cardMemory; }

    public abstract DefaultGame getGame();
    protected abstract ActionContext getRelevantContext();
    protected Map<String, String> getRelevantValueMemory() { return getRelevantContext().getValueMemory(); }
    protected Multimap<String, PhysicalCard> getRelevantCardMemory() { return getRelevantContext().getCardMemory(); }

    public void setValueToMemory(String memory, String value) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        getRelevantValueMemory().put(memory, value);
    }

    
    public String getValueFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        final String result = getRelevantValueMemory().get(memory);
        if (result == null)
            throw new IllegalArgumentException("Memory not found - " + memory);
        return result;
    }

    
    public void setCardMemory(String memory, PhysicalCard card) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        getRelevantCardMemory().removeAll(memory);
        if (card != null)
            getRelevantCardMemory().put(memory, card);
    }

    
    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        getRelevantCardMemory().removeAll(memory);
        getRelevantCardMemory().putAll(memory, cards);
    }

    public Collection<PhysicalCard> getCardsFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        return getRelevantCardMemory().get(memory);
    }

    public PhysicalCard getCardFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        final Collection<PhysicalCard> physicalCards = getRelevantCardMemory().get(memory);
        if (physicalCards.size() == 0)
            return null;
        if (physicalCards.size() != 1)
            throw new RuntimeException("Unable to retrieve one card from memory: " + memory);
        return physicalCards.iterator().next();
    }

    
    public String getPerformingPlayer() {
        return performingPlayer;
    }

    
    public PhysicalCard getSource() {
        return source;
    }

    
    public EffectResult getEffectResult() {
        return effectResult;
    }

    
    public Effect getEffect() {
        return effect;
    }

}
