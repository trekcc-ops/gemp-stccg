package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.List;

public interface ModifiersQuerying {

    LimitCounter getUntilEndOfGameLimitCounter(PhysicalCard card, String prefix);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);
    LimitCounter getUntilEndOfTurnLimitCounter(ActionBlueprint actionBlueprint);

    boolean hasIcon(PhysicalCard physicalCard, CardIcon icon);

    boolean canPlayerSolveMission(String playerId, MissionLocation mission);

    boolean hasTextRemoved(PhysicalCard card);

    Collection<Modifier> getModifiersAffecting(PhysicalCard card);

    float getAttribute(PhysicalCard card, CardAttribute attribute);

    // Stats
    float getStrength(PhysicalCard physicalCard);

    // Playing actions
    boolean canPerformAction(String performingPlayer, Action action);

    boolean canNotPlayCard(String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(PhysicalCard playedCard, PhysicalCard target);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    void appendExtraCosts(Action action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(PhysicalCard card, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(String playerId, PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);


    List<Modifier> getModifiersAffectingCard(ModifierEffect modifierEffect, PhysicalCard card);

    int getNormalCardPlaysAvailable(String playerName);

    Integer getSkillLevel(PhysicalCard physicalCard, SkillName skillName);
}