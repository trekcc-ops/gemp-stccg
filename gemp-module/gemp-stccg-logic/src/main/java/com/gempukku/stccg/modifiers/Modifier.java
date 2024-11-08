package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

@JsonSerialize(using = ModifierSerializer.class)
public interface Modifier {
    PhysicalCard getSource();

    String getCardInfoText(PhysicalCard affectedCard);

    ModifierEffect getModifierEffect();

    boolean isNonCardTextModifier();
    boolean isForPlayer(String playerId);

    Condition getCondition();
    boolean isCumulative();

    boolean affectsCard(PhysicalCard physicalCard);

    boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard);

    int getAttributeModifier(PhysicalCard physicalCard);

    boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget);

    boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType);

    boolean canPlayAction(DefaultGame game, String performingPlayer, Action action);

    boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card);

    List<? extends Action> getExtraPhaseAction(DefaultGame game, PhysicalCard card);

    List<? extends Action> getExtraPhaseActionFromStacked(DefaultGame game, PhysicalCard card);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card);

    void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card);

    boolean canHavePlayedOn(DefaultGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId);

    boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(DefaultGame game, PhysicalCard card, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId);

    boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source);
    boolean canPlayCardOutOfSequence(PhysicalCard source);

    boolean hasFlagActive(ModifierFlag modifierFlag);

    boolean hasIcon(PhysicalCard card, CardIcon icon);

    String getForPlayer();
    String getText();
}