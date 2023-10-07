package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.DefaultGame;

public interface Action<AbstractGame extends DefaultGame> {
    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, RECONCILE, RESOLVE_DAMAGE, OTHER,
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

    Effect<AbstractGame> nextEffect(AbstractGame game);
}
