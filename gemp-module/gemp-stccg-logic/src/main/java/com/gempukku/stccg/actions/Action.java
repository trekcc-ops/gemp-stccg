package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface Action {
    String getCardActionPrefix();

    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, OTHER,
        MOVE_CARDS, ACTIVATE_TRIBBLE_POWER, ATTEMPT_MISSION,
        BATTLE, SEED_CARD
    }

    ActionType getActionType();

    PhysicalCard getActionSource();

    PhysicalCard getActionAttachedToCard();

    void setVirtualCardAction(boolean virtualCardAction);

    boolean isVirtualCardAction();

    String getPerformingPlayerId();

    String getText();

    Effect nextEffect() throws InvalidGameLogicException;
    SubAction createSubAction();
    DefaultGame getGame();
    boolean canBeInitiated();
}
