package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Modifier {
    PhysicalCard getSource();

    String getCardInfoText(PhysicalCard affectedCard);

    ModifierEffect getModifierEffect();

    boolean isNonCardTextModifier();

    Condition getCondition();
    boolean isCumulative();
    String getForPlayer();
    boolean isForPlayer(String playerId);

    boolean affectsCard(PhysicalCard physicalCard);

    boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard);

    boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCountModifier(PhysicalCard physicalCard, Keyword keyword);

    boolean appliesKeywordModifier(DefaultGame game, PhysicalCard modifierSource, Keyword keyword);

    boolean isKeywordRemoved(DefaultGame game, PhysicalCard physicalCard, Keyword keyword);

    int getStrengthModifier(PhysicalCard physicalCard);

    boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget);

    int getVitalityModifier(DefaultGame game, PhysicalCard physicalCard);

    int getResistanceModifier(PhysicalCard physicalCard);

    int getMinionSiteNumberModifier(DefaultGame game, PhysicalCard physicalCard);

    boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType);

    int getTwilightCostModifier(PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty);

    int getRoamingPenaltyModifier(DefaultGame game, PhysicalCard physicalCard);

    int getOverwhelmMultiplier(DefaultGame game, PhysicalCard physicalCard);

    boolean canCancelSkirmish(DefaultGame game, PhysicalCard physicalCard);

    boolean canTakeWounds(DefaultGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTakenInPhase, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, PhysicalCard physicalCard, Set<PhysicalCard> winners);

    boolean canTakeArcheryWound(DefaultGame game, PhysicalCard physicalCard);

    boolean canBeExerted(DefaultGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    int getMoveLimitModifier();

    boolean addsTwilightForCompanionMove(DefaultGame game, PhysicalCard companion);

    boolean addsToArcheryTotal(DefaultGame game, PhysicalCard card);

    boolean canPlayAction(DefaultGame game, String performingPlayer, Action action);

    boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card);

    List<? extends Action> getExtraPhaseAction(DefaultGame game, PhysicalCard card);

    List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card);

    void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card);

    boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(PhysicalCard attachment);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeLiberated(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source);

    boolean canBeHealed(DefaultGame game, PhysicalCard card);

    boolean canAddBurden(DefaultGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(DefaultGame game, PhysicalCard source);

    boolean canRemoveThreat(DefaultGame game, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId);

    boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source);
    boolean canPlayCardOutOfSequence(PhysicalCard source);

    int getSpotCountModifier(DefaultGame game, Filterable filter);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    boolean isSiteReplaceable(DefaultGame game, String playerId);

    boolean canPlaySite(DefaultGame game, String playerId);

    boolean shadowCanHaveInitiative(DefaultGame game);
    boolean hasIcon(PhysicalCard card, CardIcon icon);

    int getInitiativeHandSizeModifier(DefaultGame game);

    boolean lostAllKeywords(PhysicalCard card);

    void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard card);
}
