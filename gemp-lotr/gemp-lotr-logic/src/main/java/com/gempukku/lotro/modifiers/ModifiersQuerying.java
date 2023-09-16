package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModifiersQuerying {
    LimitCounter getUntilEndOfPhaseLimitCounter(LotroPhysicalCard card, Phase phase);

    LimitCounter getUntilEndOfPhaseLimitCounter(LotroPhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(LotroPhysicalCard card, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(LotroPhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilEndOfTurnLimitCounter(LotroPhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(LotroPhysicalCard card, String prefix);

    Collection<Modifier> getModifiersAffecting(DefaultGame game, LotroPhysicalCard card);

    Evaluator getFPStrengthOverrideEvaluator(DefaultGame game, LotroPhysicalCard fpCharacter);
    Evaluator getShadowStrengthOverrideEvaluator(DefaultGame game, LotroPhysicalCard fpCharacter);

    boolean hasTextRemoved(DefaultGame game, LotroPhysicalCard card);

    // Keywords
    boolean hasKeyword(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword);

    boolean hasSignet(DefaultGame game, LotroPhysicalCard physicalCard, Signet signet);

    // Archery
    int getArcheryTotal(DefaultGame game, Side side, int baseArcheryTotal);

    boolean addsToArcheryTotal(DefaultGame game, LotroPhysicalCard card);

    // Movement
    int getMoveLimit(DefaultGame game, int baseMoveLimit);

    boolean addsTwilightForCompanionMove(DefaultGame game, LotroPhysicalCard companion);

    // Twilight cost
    int getTwilightCost(DefaultGame game, LotroPhysicalCard physicalCard, LotroPhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);

    int getRoamingPenalty(DefaultGame game, LotroPhysicalCard physicalCard);

    // Stats
    int getStrength(DefaultGame game, LotroPhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(DefaultGame game, LotroPhysicalCard modifierSource, LotroPhysicalCard modifierTarget);

    int getVitality(DefaultGame game, LotroPhysicalCard physicalCard);

    int getResistance(DefaultGame game, LotroPhysicalCard physicalCard);

    int getMinionSiteNumber(DefaultGame game, LotroPhysicalCard physicalCard);

    int getOverwhelmMultiplier(DefaultGame game, LotroPhysicalCard card);

    boolean isAdditionalCardType(DefaultGame game, LotroPhysicalCard card, CardType cardType);

    // Wounds/exertions
    boolean canTakeWounds(DefaultGame game, Collection<LotroPhysicalCard> woundSources, LotroPhysicalCard card, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, LotroPhysicalCard card, Set<LotroPhysicalCard> winners);

    boolean canTakeArcheryWound(DefaultGame game, LotroPhysicalCard card);

    boolean canBeExerted(DefaultGame game, LotroPhysicalCard exertionSource, LotroPhysicalCard exertedCard);

    boolean canBeHealed(DefaultGame game, LotroPhysicalCard card);

    boolean canAddBurden(DefaultGame game, String performingPlayer, LotroPhysicalCard source);

    boolean canRemoveBurden(DefaultGame game, LotroPhysicalCard source);

    boolean canRemoveThreat(DefaultGame game, LotroPhysicalCard source);

    // Assignments
    boolean canBeAssignedToSkirmish(DefaultGame game, Side playerSide, LotroPhysicalCard card);

    boolean canCancelSkirmish(DefaultGame game, LotroPhysicalCard card);

    boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(DefaultGame game, LotroPhysicalCard card);

    boolean isAllyAllowedToParticipateInArcheryFire(DefaultGame game, LotroPhysicalCard card);

    boolean isAllyAllowedToParticipateInSkirmishes(DefaultGame game, Side sidePlayer, LotroPhysicalCard card);

    boolean isAllyPreventedFromParticipatingInSkirmishes(DefaultGame game, Side sidePlayer, LotroPhysicalCard card);

    boolean isValidAssignments(DefaultGame game, Side side, Map<LotroPhysicalCard, Set<LotroPhysicalCard>> assignments);

    // Playing actions
    boolean canPlayAction(DefaultGame game, String performingPlayer, Action action);

    boolean canPlayCard(DefaultGame game, String performingPlayer, LotroPhysicalCard card);

    boolean canHavePlayedOn(DefaultGame game, LotroPhysicalCard playedCard, LotroPhysicalCard target);

    boolean canHaveTransferredOn(DefaultGame game, LotroPhysicalCard playedCard, LotroPhysicalCard target);

    boolean canBeTransferred(DefaultGame game, LotroPhysicalCard attachment);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, LotroPhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, LotroPhysicalCard target);

    boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard target);

    void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, LotroPhysicalCard card, LotroPhysicalCard source);

    boolean canBeReturnedToHand(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard source);

    boolean canDrawCardNoIncrement(DefaultGame game, String playerId);

    boolean canDrawCardAndIncrementForRuleOfFour(DefaultGame game, String playerId);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(DefaultGame game, String playerId, LotroPhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, LotroPhysicalCard source);

    boolean canBeLiberated(DefaultGame game, String playerId, LotroPhysicalCard card, LotroPhysicalCard source);

    Side hasInitiative(DefaultGame game);

    int getNumberOfSpottableFPCultures(DefaultGame game, String playerId);

    int getNumberOfSpottableShadowCultures(DefaultGame game, String playerId);

    int getSpotBonus(DefaultGame game, Filterable filter);

    boolean hasFlagActive(DefaultGame game, ModifierFlag modifierFlag);

    boolean canReplaceSite(DefaultGame game, String playerId, LotroPhysicalCard siteToReplace);

    boolean canPlaySite(DefaultGame game, String playerId);

    int getSanctuaryHealModifier(DefaultGame game);

    int getPotentialDiscount(DefaultGame game, LotroPhysicalCard playedCard);

    void appendPotentialDiscounts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard playedCard);
}
