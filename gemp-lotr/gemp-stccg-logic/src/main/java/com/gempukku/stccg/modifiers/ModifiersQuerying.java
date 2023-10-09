package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModifiersQuerying {
    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    Collection<Modifier> getModifiersAffecting(DefaultGame game, PhysicalCard card);

    boolean hasTextRemoved(DefaultGame game, PhysicalCard card);

    // Keywords
    boolean hasKeyword(DefaultGame game, PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(DefaultGame game, PhysicalCard physicalCard, Keyword keyword);

    // Archery
    int getArcheryTotal(DefaultGame game, Side side, int baseArcheryTotal);

    // Movement
    int getMoveLimit(DefaultGame game, int baseMoveLimit);

    boolean addsTwilightForCompanionMove(DefaultGame game, PhysicalCard companion);

    // Twilight cost
    int getTwilightCost(DefaultGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);

    int getRoamingPenalty(DefaultGame game, PhysicalCard physicalCard);

    // Stats
    int getStrength(DefaultGame game, PhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget);

    int getResistance(DefaultGame game, PhysicalCard physicalCard);

    int getOverwhelmMultiplier(DefaultGame game, PhysicalCard card);

    boolean isAdditionalCardType(DefaultGame game, PhysicalCard card, CardType cardType);

    // Wounds/exertions
    boolean canTakeWounds(DefaultGame game, Collection<PhysicalCard> woundSources, PhysicalCard card, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, PhysicalCard card, Set<PhysicalCard> winners);

    boolean canBeExerted(DefaultGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    boolean canBeHealed(DefaultGame game, PhysicalCard card);

    boolean canAddBurden(DefaultGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(DefaultGame game, PhysicalCard source);

    boolean canRemoveThreat(DefaultGame game, PhysicalCard source);

    // Assignments
    boolean canBeAssignedToSkirmish(DefaultGame game, Side playerSide, PhysicalCard card);

    boolean canCancelSkirmish(DefaultGame game, PhysicalCard card);

    boolean isValidAssignments(DefaultGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    // Playing actions
    boolean canPlayAction(DefaultGame game, String performingPlayer, Action action);

    boolean canNotPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(DefaultGame game, PhysicalCard attachment);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, PhysicalCard target);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard target);

    void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source);

    boolean canDrawCardNoIncrement(DefaultGame game, String playerId);
    boolean canDrawCardAndIncrementForRuleOfFour(DefaultGame game, String playerId);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source);

    boolean canBeLiberated(DefaultGame game, String playerId, PhysicalCard card, PhysicalCard source);

    Side hasInitiative(DefaultGame game);

    int getNumberOfSpottableFPCultures(DefaultGame game, String playerId);

    int getNumberOfSpottableShadowCultures(DefaultGame game, String playerId);

    int getSpotBonus(DefaultGame game, Filterable filter);

    boolean hasFlagActive(DefaultGame game, ModifierFlag modifierFlag);

    boolean canReplaceSite(DefaultGame game, String playerId, PhysicalCard siteToReplace);

    boolean canPlaySite(DefaultGame game, String playerId);

    int getPotentialDiscount(DefaultGame game, PhysicalCard playedCard);

    void appendPotentialDiscounts(DefaultGame game, CostToEffectAction action, PhysicalCard playedCard);
}
