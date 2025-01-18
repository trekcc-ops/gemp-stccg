package com.gempukku.stccg.cards;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class DefaultActionContext implements ActionContext {
    private final DefaultGame _game;
    protected final ActionContext _relevantContext;
    protected final String performingPlayer;
    protected final PhysicalCard source;
    protected final ActionResult actionResult;
    protected final Multimap<String, PhysicalCard> _cardMemory = HashMultimap.create();
    protected final Map<String, String> _valueMemory = new HashMap<>();

    public DefaultActionContext(String performingPlayer, DefaultGame game, PhysicalCard source,
                                ActionResult actionResult) {
        this(null, performingPlayer, game, source, actionResult);
    }

    public DefaultActionContext(String performingPlayer, PhysicalCard source, ActionResult actionResult) {
        this(null, performingPlayer, source.getGame(), source, actionResult);
    }



    public DefaultActionContext(ActionContext delegate, String performingPlayer, DefaultGame game,
                                PhysicalCard source, ActionResult actionResult) {
        this.performingPlayer = performingPlayer;
        this.source = source;
        this.actionResult = actionResult;
        _game = game;
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    public ActionContext createDelegateContext(ActionResult actionResult) {
        return new DefaultActionContext(this, getPerformingPlayerId(), getGame(), getSource(), actionResult);
    }

    public ActionContext createDelegateContext(String playerId) {
        return new DefaultActionContext(this, playerId, getGame(), getSource(), getEffectResult());
    }
    public Map<String, String> getValueMemory() { return _valueMemory; }
    public Multimap<String, PhysicalCard> getCardMemory() { return _cardMemory; }
    public Player getPerformingPlayer() { return _game.getGameState().getPlayer(performingPlayer); }
    public String getPerformingPlayerId() { return performingPlayer; }
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




    protected ActionContext getRelevantContext() { return _relevantContext; }
    protected Map<String, String> getRelevantValueMemory() { return getRelevantContext().getValueMemory(); }
    protected Multimap<String, PhysicalCard> getRelevantCardMemory() { return getRelevantContext().getCardMemory(); }


    @Override
    public DefaultGame getGame() {
        return _game;
    }

    @Override
    public GameState getGameState() { return _game.getGameState(); }


    public ActionResult getEffectResult() {
        return actionResult;
    }


    public boolean acceptsAllRequirements(Requirement[] requirementArray) {
        boolean result = true;
        for (Requirement requirement : requirementArray) {
            if (!requirement.accepts(this)) result = false;
        }
        return result;
    }

    public boolean acceptsAllRequirements(List<Requirement> requirementList) {
        boolean result = true;
        for (Requirement requirement : requirementList) {
            if (!requirement.accepts(this)) result = false;
        }
        return result;
    }

    public boolean acceptsAnyRequirements(Requirement[] requirementArray) {
        return Arrays.stream(requirementArray).anyMatch(requirement -> requirement.accepts(this));
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

    public List<PhysicalCard> getZoneCards(PlayerSource playerSource, Zone zone) {
        return _game.getGameState().getZoneCards(playerSource.getPlayerId(this), zone);
    }

    public ActionContext getParentContext() {
        return _relevantContext;
    }

}