package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
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

    Collection<Modifier> getModifiersAffecting(PhysicalCard card);

    // Keywords
    boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(PhysicalCard physicalCard, Keyword keyword);

    // Archery
    int getArcheryTotal(Side side, int baseArcheryTotal);

    // Movement
    int getMoveLimit(int baseMoveLimit);

    boolean addsTwilightForCompanionMove(PhysicalCard companion);

    // Twilight cost
    int getTwilightCost(DefaultGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);

    int getRoamingPenalty(DefaultGame game, PhysicalCard physicalCard);

    // Stats
    int getStrength(PhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(PhysicalCard modifierSource, PhysicalCard modifierTarget);

    boolean isAdditionalCardType(DefaultGame game, PhysicalCard card, CardType cardType);

    // Wounds/exertions
    boolean canTakeWounds(DefaultGame game, Collection<PhysicalCard> woundSources, PhysicalCard card, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, PhysicalCard card, Set<PhysicalCard> winners);

    boolean canBeExerted(DefaultGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    boolean canBeHealed(PhysicalCard card);

    boolean canAddBurden(DefaultGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(DefaultGame game, PhysicalCard source);

    boolean canRemoveThreat(DefaultGame game, PhysicalCard source);

    boolean canCancelSkirmish(DefaultGame game, PhysicalCard card);

    boolean isValidAssignments(DefaultGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    // Playing actions
    boolean canPlayAction(String performingPlayer, Action action);

    boolean canNotPlayCard(String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(DefaultGame game, PhysicalCard attachment);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, PhysicalCard target);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard target);

    void appendExtraCosts(CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source);

    boolean canDrawCardNoIncrement(DefaultGame game, String playerId);
    boolean canDrawCardAndIncrementForRuleOfFour(DefaultGame game, String playerId);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source);

    boolean canBeLiberated(DefaultGame game, String playerId, PhysicalCard card, PhysicalCard source);

    Side hasInitiative();

    int getNumberOfSpottableFPCultures(DefaultGame game, String playerId);

    int getNumberOfSpottableShadowCultures(DefaultGame game, String playerId);

    int getSpotBonus(Filterable filter);

    boolean hasFlagActive(DefaultGame game, ModifierFlag modifierFlag);

    boolean canReplaceSite(String playerId, PhysicalCard siteToReplace);

    boolean canPlaySite(String playerId);

    int getPotentialDiscount(PhysicalCard playedCard);

    void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard playedCard);
    List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card);

}
