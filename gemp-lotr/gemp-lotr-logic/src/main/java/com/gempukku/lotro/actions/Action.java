package com.gempukku.lotro.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.Effect;

public interface Action<AbstractGame extends DefaultGame> {
    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, RECONCILE, RESOLVE_DAMAGE, OTHER,
        TRIBBLE_POWER
    }

    ActionType getActionType();

    PhysicalCard getActionSource();

    void setActionTimeword(Phase phase);

    PhysicalCard getActionAttachedToCard();

    void setVirtualCardAction(boolean virtualCardAction);

    boolean isVirtualCardAction();

    void setPerformingPlayer(String playerId);

    String getPerformingPlayer();

    Phase getActionTimeword();

    String getText(DefaultGame game);

    Effect<AbstractGame> nextEffect(AbstractGame game);
}
