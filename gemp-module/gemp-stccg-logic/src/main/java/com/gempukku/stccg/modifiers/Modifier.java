package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

public interface Modifier {
    PhysicalCard getSource();

    String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard);

    ModifierEffect getModifierEffect();

    boolean isNonCardTextModifier();
    boolean isForPlayer(String playerId);

    Condition getCondition();
    boolean isCumulative();
    boolean isConditionFulfilled(DefaultGame cardGame);

    boolean affectsCard(DefaultGame cardGame, PhysicalCard physicalCard);

    boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard);

    float getAttributeModifier(DefaultGame cardGame, PhysicalCard physicalCard);

    boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget);

    boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType);

    boolean canPerformAction(DefaultGame game, String performingPlayer, Action action);

    boolean cantPlayCard(DefaultGame game, String performingPlayer, PhysicalCard card);

    boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card);

    void appendExtraCosts(DefaultGame game, Action action, PhysicalCard card);

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

    default boolean foundNoCumulativeConflict(Iterable<Modifier> modifierList) {
        // If modifier is not cumulative, then check if modifiers from another copy
        // card of same title is already in the list
        if (!isCumulative() && getSource() != null) {

            ModifierEffect modifierEffect = getModifierEffect();
            String cardTitle = getSource().getTitle();
            String forPlayer = getForPlayer();

            for (Modifier otherModifier : modifierList) {
                // check for the same effect from a copy of the same card
                if (otherModifier.getModifierEffect() == modifierEffect && otherModifier.getSource() != null &&
                        otherModifier.getSource().getTitle().equals(cardTitle) && otherModifier.isForPlayer(forPlayer)) {
                    return false;
                }
            }
        }
        return true;
    }

}