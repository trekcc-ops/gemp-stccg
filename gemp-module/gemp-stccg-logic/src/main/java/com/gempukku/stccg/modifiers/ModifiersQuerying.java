package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.List;

public interface ModifiersQuerying {
    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    boolean hasIcon(PhysicalCard physicalCard, CardIcon icon);

    boolean hasTextRemoved(PhysicalCard card);

    Collection<Modifier> getModifiersAffecting(PhysicalCard card);

    // Keywords
    boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(PhysicalCard physicalCard, Keyword keyword);

    // Movement
    int getMoveLimit(int baseMoveLimit);

    int getAttribute(PhysicalCard card, CardAttribute attribute);

    // Stats
    int getStrength(PhysicalCard physicalCard);

    // Playing actions
    boolean canPlayAction(String performingPlayer, Action action);

    boolean canNotPlayCard(String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target);

    void appendExtraCosts(CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source);

    boolean canDrawCardNoIncrement(String playerId);

    boolean canLookOrRevealCardsInHand(String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    void appendPotentialDiscounts(CostToEffectAction action, PhysicalCard playedCard);

    LimitCounter getUntilEndOfTurnLimitCounter(ActionSource actionSource);

    List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card);
    int getNormalCardPlaysAvailable(Player player);

}
