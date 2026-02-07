package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

import java.util.Objects;

public abstract class AbstractModifier implements Modifier {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    protected final PhysicalCard _cardSource;
    @JsonProperty("affectedCards")
    private final CardFilter _affectedCardsFilter;
    @JsonProperty("condition")
    protected final Condition _condition;
    @JsonProperty("effectType")
    private final ModifierEffect _effect;

    protected AbstractModifier(PhysicalCard source, CardFilter affectedCards, Condition condition,
                               ModifierEffect effect) {
        _cardSource = source;
        _affectedCardsFilter = Objects.requireNonNullElse(affectedCards, Filters.any);
        _condition = Objects.requireNonNullElse(condition, new TrueCondition());
        _effect = effect;
    }


    protected AbstractModifier(CardFilter affectFilter, Condition condition, ModifierEffect effect) {
        this(null, affectFilter, condition, effect);
    }


    @JsonIgnore
    @Override
    public Condition getCondition() {
        return _condition;
    }

    @JsonIgnore
    @Override
    public PhysicalCard getSource() {
        return _cardSource;
    }

    abstract public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard);

    @JsonIgnore
    @Override
    public ModifierEffect getModifierType() {
        return _effect;
    }

    @Override
    public boolean affectsCard(DefaultGame cardGame, PhysicalCard physicalCard) {
        return _affectedCardsFilter.accepts(cardGame, physicalCard);
    }


    @Override
    public boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard) {
        return false;
    }

    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        return false;
    }

    @Override
    public float getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean canPerformAction(DefaultGame game, String performingPlayer, Action action) {
        return true;
    }

    @Override
    public boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, Action action, PhysicalCard card) {

    }

    @Override
    public boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return false;
    }

    @JsonIgnore
    public boolean isCumulative() { return true; }

    public final boolean isConditionFulfilled(DefaultGame cardGame) {
        return _condition == null || _condition.isFulfilled(cardGame);
    }

    public final boolean foundNoCumulativeConflict(Iterable<Modifier> modifierList) {
        // If modifier is not cumulative, then check if modifiers from another copy
        // card of same title is already in the list
        if (!isCumulative() && _cardSource != null) {

            ModifierEffect modifierEffect = getModifierType();
            String cardTitle = _cardSource.getTitle();

            for (Modifier otherModifier : modifierList) {
                // check for the same effect from a copy of the same card
                if (otherModifier.getModifierType() == modifierEffect && otherModifier.getSource() != null &&
                        otherModifier.getSource().getTitle().equals(cardTitle)) {
                    return false;
                }
            }
        }
        return true;
    }

    @JsonIgnore
    public boolean isSuspended(DefaultGame cardGame) {
        if (_cardSource == null) {
            return false;
        } else {
            return _cardSource.hasTextRemoved(cardGame);
        }
    }

    public boolean isEffectType(ModifierEffect effectType) {
        return _effect == effectType;
    }


}