package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.common.filterable.lotr.Side;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractModifier implements Modifier {
    protected String _playerId;
    protected final PhysicalCard _cardSource;
    private final String _text;
    protected final Filter _affectFilter;
    protected final Condition _condition;
    private final ModifierEffect _effect;
    protected final DefaultGame _game;

    protected AbstractModifier(PhysicalCard source, String text, Filterable affectFilter, ModifierEffect effect) {
        this(source, text, affectFilter, null, effect);
    }

    protected AbstractModifier(PhysicalCard source, Filterable affectFilter, Condition condition, ModifierEffect effect) {
        this(source, null, affectFilter, condition, effect);
    }

    protected AbstractModifier(DefaultGame game, Filterable affectFilter, Condition condition, ModifierEffect effect) {
        _cardSource = null;
        _text = null;
        _affectFilter = (affectFilter != null) ? Filters.and(affectFilter) : null;
        _condition = null;
        _effect = effect;
        _game = game;
    }

    protected AbstractModifier(PhysicalCard source, String text, Filterable affectFilter,
                               Condition condition, ModifierEffect effect) {
        _cardSource = source;
        _text = text;
        _affectFilter = (affectFilter != null) ? Filters.and(affectFilter) : null;
        _condition = condition;
        _effect = effect;
        _game = source.getGame();
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
    public String getCardInfoText(PhysicalCard affectedCard) {
        return _text;
    }

    @Override
    public ModifierEffect getModifierEffect() {
        return _effect;
    }

    @Override
    public boolean affectsCard(PhysicalCard physicalCard) {
        return (_affectFilter != null && _affectFilter.accepts(_game, physicalCard));
    }

    @Override
    public boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard) {
        return false;
    }

    @Override
    public boolean isKeywordRemoved(DefaultGame game, PhysicalCard physicalCard, Keyword keyword) {
        return false;
    }

    @Override
    public boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword) {
        return false;
    }
    @Override
    public boolean hasIcon(PhysicalCard physicalCard, CardIcon icon) {
        return false;
    }

    @Override
    public int getKeywordCountModifier(PhysicalCard physicalCard, Keyword keyword) {
        return 0;
    }

    @Override
    public boolean appliesKeywordModifier(DefaultGame game, PhysicalCard modifierSource, Keyword keyword) {
        return true;
    }

    @Override
    public int getStrengthModifier(PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget) {
        return false;
    }

    @Override
    public int getVitalityModifier(DefaultGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public int getResistanceModifier(PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public int getMinionSiteNumberModifier(DefaultGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType) {
        return false;
    }

    @Override
    public int getTwilightCostModifier(PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return 0;
    }

    @Override
    public int getOverwhelmMultiplier(DefaultGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean canCancelSkirmish(DefaultGame game, PhysicalCard physicalCard) { return true; }

    @Override
    public boolean canTakeWounds(DefaultGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        return true;
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, PhysicalCard physicalCard, Set<PhysicalCard> winners) {
        return true;
    }

    @Override
    public boolean canTakeArcheryWound(DefaultGame game, PhysicalCard physicalCard) {
        return true;
    }

    @Override
    public boolean canBeExerted(DefaultGame game, PhysicalCard exertionSource, PhysicalCard exertedCard) {
        return true;
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(DefaultGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(DefaultGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInSkirmishes(DefaultGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public int getArcheryTotalModifier(DefaultGame game, Side side) {
        return 0;
    }

    @Override
    public int getMoveLimitModifier() {
        return 0;
    }

    @Override
    public boolean addsTwilightForCompanionMove(DefaultGame game, PhysicalCard companion) {
        return true;
    }

    @Override
    public boolean addsToArcheryTotal(DefaultGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        return true;
    }

    @Override
    public boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public List<? extends Action> getExtraPhaseAction(DefaultGame game, PhysicalCard card) {
        return null;
    }

    @Override
    public List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card) {
        return null;
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {

    }

    @Override
    public boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        return true;
    }

    @Override
    public boolean canHaveTransferredOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target) {
        return true;
    }

    @Override
    public boolean canBeTransferred(PhysicalCard attachment) {
        return true;
    }

    @Override
    public boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId) {
        return false;
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(DefaultGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeLiberated(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeHealed(DefaultGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public boolean canAddBurden(DefaultGame game, String performingPlayer, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canRemoveBurden(DefaultGame game, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canRemoveThreat(DefaultGame game, PhysicalCard source) {
        return true;
    }

    @Override
    public int getRoamingPenaltyModifier(DefaultGame game, PhysicalCard physicalCard) {
        return 0;
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
    public int getSpotCountModifier(DefaultGame game, Filterable filter) {
        return 0;
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return false;
    }

    @Override
    public boolean isSiteReplaceable(DefaultGame game, String playerId) {
        return true;
    }

    @Override
    public boolean canPlaySite(DefaultGame game, String playerId) {
        return true;
    }

    @Override
    public boolean shadowCanHaveInitiative(DefaultGame game) {
        return true;
    }

    @Override
    public Side hasInitiative() {
        return null;
    }

    @Override
    public int getInitiativeHandSizeModifier(DefaultGame game) {
        return 0;
    }

    @Override
    public boolean lostAllKeywords(PhysicalCard card) {
        return false;
    }

    @Override
    public int getFPCulturesSpotCountModifier(DefaultGame game, String playerId) {
        return 0;
    }

    @Override
    public void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard card) {

    }

    public String getForPlayer() {
        return _playerId;
    }

    public boolean isForPlayer(String playerId) {
        return _playerId == null || _playerId.equals(playerId);
    }

    public boolean isCumulative() { return true; }

}
