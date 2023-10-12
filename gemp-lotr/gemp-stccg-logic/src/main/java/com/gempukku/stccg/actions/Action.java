package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;

public interface Action {
    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, OTHER,
        TRIBBLE_POWER
    }

    ActionType getActionType();

    PhysicalCard getActionSource();

    PhysicalCard getActionAttachedToCard();

    void setVirtualCardAction(boolean virtualCardAction);

    boolean isVirtualCardAction();

    void setPerformingPlayer(String playerId);

    String getPerformingPlayer();

    String getText();

    Effect nextEffect(DefaultGame game);
}
