package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.List;

public interface ModifiersQuerying {

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    boolean hasIcon(PhysicalCard physicalCard, CardIcon icon);
    boolean canPlayerSolveMission(String playerId, MissionCard mission);

    boolean hasTextRemoved(PhysicalCard card);

    Collection<Modifier> getModifiersAffecting(PhysicalCard card);

    int getAttribute(PhysicalCard card, CardAttribute attribute);

    // Stats
    int getStrength(PhysicalCard physicalCard);

    // Playing actions
    boolean canPerformAction(String performingPlayer, Action action);

    boolean canNotPlayCard(String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(DefaultGame game, PhysicalCard target);

    void appendExtraCosts(Action action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    LimitCounter getUntilEndOfTurnLimitCounter(ActionSource actionSource);

    List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card);
    int getNormalCardPlaysAvailable(Player player);

    Integer getSkillLevel(PhysicalCard physicalCard, SkillName skillName);
}