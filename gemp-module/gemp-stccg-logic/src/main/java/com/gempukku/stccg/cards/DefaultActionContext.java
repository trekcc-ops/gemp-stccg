package com.gempukku.stccg.cards;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultActionContext implements ActionContext {
    protected final ActionContext _relevantContext;
    protected final String _performingPlayerName;
    protected final PhysicalCard source;
    protected final ActionResult actionResult;
    protected final Multimap<String, PhysicalCard> _cardMemory = HashMultimap.create();
    protected final Map<String, String> _valueMemory = new HashMap<>();

    public DefaultActionContext(ActionContext delegate, String performingPlayer,
                                PhysicalCard source, ActionResult actionResult) {
        this._performingPlayerName = performingPlayer;
        this.source = source;
        this.actionResult = actionResult;
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    public DefaultActionContext(PhysicalCard thisCard, String performingPlayerId) {
        this(null, performingPlayerId, thisCard, null);
    }

    public DefaultActionContext(String performingPlayer, PhysicalCard source, ActionResult actionResult) {
        this(null, performingPlayer, source, actionResult);
    }

    public Map<String, String> getValueMemory() { return _valueMemory; }
    public Multimap<String, PhysicalCard> getCardMemory() { return _cardMemory; }

    public String getPerformingPlayerId() { return _performingPlayerName; }
    public PhysicalCard getSource() {
        return source;
    }

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
        if (physicalCards.isEmpty())
            return null;
        if (physicalCards.size() != 1)
            throw new RuntimeException("Unable to retrieve one card from memory: " + memory);
        return physicalCards.iterator().next();
    }


    protected Map<String, String> getRelevantValueMemory() { return _relevantContext.getValueMemory(); }
    protected Multimap<String, PhysicalCard> getRelevantCardMemory() { return _relevantContext.getCardMemory(); }


    public ActionResult getEffectResult() {
        return actionResult;
    }

    public boolean hasActionResultType(ActionResult.Type type) {
        return actionResult != null && actionResult.getType() == type;
    }

    public boolean acceptsAllRequirements(DefaultGame cardGame, Iterable<Requirement> requirements) {
        if (requirements == null)
            return true;
        boolean result = true;
        for (Requirement requirement : requirements) {
            if (!requirement.accepts(this, cardGame)) result = false;
        }
        return result;
    }

    public String substituteText(String text) {
        String result = text;
        while (result.contains("{")) {
            int startIndex = result.indexOf("{");
            int endIndex = result.indexOf("}");
            String memory = result.substring(startIndex + 1, endIndex);
            String cardNames = TextUtils.getConcatenatedCardLinks(getCardsFromMemory(memory));
            if (cardNames.equalsIgnoreCase("none")) {
                try {
                    cardNames = getValueFromMemory(memory);
                } catch (IllegalArgumentException ex) {
                    cardNames = "none";
                }
            }
            result = result.replace("{" + memory + "}", cardNames);
        }
        return result;
    }


}