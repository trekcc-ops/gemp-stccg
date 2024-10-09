package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.sources.ActionSource;
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

    int getAttribute(PhysicalCard card, CardAttribute attribute);

    // Stats
    int getStrength(PhysicalCard physicalCard);

    boolean isAdditionalCardType(DefaultGame game, PhysicalCard card, CardType cardType);

    // Playing actions
    boolean canPlayAction(String performingPlayer, Action action);

    boolean canNotPlayCard(String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, PhysicalCard target);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard target);

    void appendExtraCosts(CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    LimitCounter getUntilEndOfTurnLimitCounter(ActionSource actionSource);

    List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card);
    int getNormalCardPlaysAvailable(Player player);

}
