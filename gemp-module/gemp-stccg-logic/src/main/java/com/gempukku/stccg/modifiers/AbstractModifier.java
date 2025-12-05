package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;
import com.gempukku.stccg.requirement.TrueCondition;

public abstract class AbstractModifier implements Modifier {
    protected final PhysicalCard _cardSource;
    private final String _text;
    protected String _playerId;
    private final CardFilter _affectedCardsFilter;
    protected final Condition _condition;
    private final ModifierEffect _effect;

    protected AbstractModifier(PhysicalCard source, String text, Filterable affectFilter,
                               Condition condition, ModifierEffect effect) {
        _cardSource = source;
        _text = text;
        _affectedCardsFilter = (affectFilter == null) ? Filters.any : Filters.changeToFilter(affectFilter);
        _condition = condition;
        _effect = effect;
    }

    protected AbstractModifier(PhysicalCard source, CardFilter affectedCards, Condition condition,
                               ModifierEffect effect) {
        this(source, null, affectedCards, condition, effect);
    }

    protected AbstractModifier(PhysicalCard performingCard, ModifierEffect effect) {
        this(performingCard, null, Filters.any, null, effect);
    }


    protected AbstractModifier(ModifierEffect effect) {
        this(null, null, Filters.any, null, effect);
    }

    protected AbstractModifier(String text, Filterable affectFilter, ModifierEffect effect) {
        this(null, text, affectFilter, new TrueCondition(), effect);
    }


    protected AbstractModifier(PhysicalCard source, String text, Filterable affectFilter, ModifierEffect effect) {
        this(source, text, affectFilter, null, effect);
    }

    protected AbstractModifier(PhysicalCard source, Filterable affectFilter, Condition condition,
                               ModifierEffect effect) {
        this(source, null, affectFilter, condition, effect);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return false;
    }

    @Override
    public Condition getCondition() {
        return _condition;
    }

    @Override
    public PhysicalCard getSource() {
        return _cardSource;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return _text;
    }

    @Override
    public ModifierEffect getModifierEffect() {
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
    public boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource,
                                                PhysicalCard modifierTarget) {
        return false;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType) {
        return false;
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
    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, Action action, PhysicalCard card) {

    }

    @Override
    public boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        return true;
    }

    @Override
    public boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId) {
        return false;
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card,
                                          PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId) {
        return true;
    }

    @Override
    public boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source) {
        return true;
    }

    public boolean canPlayCardOutOfSequence(PhysicalCard source) { return false; }
    @Override
    public boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return false;
    }

    public boolean isCumulative() { return true; }

    public boolean isForPlayer(String playerId) {
        return _playerId == null || _playerId.equals(playerId);
    }

    public String getForPlayer() { return _playerId; }
    public String getText() { return _text; }

}