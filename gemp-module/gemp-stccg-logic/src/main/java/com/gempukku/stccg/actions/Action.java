package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.SnapshotData;
import com.gempukku.stccg.game.Snapshotable;

public interface Action extends Snapshotable<Action> {
    String getCardActionPrefix();

    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, OTHER,
        MOVE_CARDS, ACTIVATE_TRIBBLE_POWER, ATTEMPT_MISSION,
        BATTLE, SEED_CARD
    }

    ActionType getActionType();

    PhysicalCard getActionSource();

    PhysicalCard getCardForActionSelection();

    void setVirtualCardAction(boolean virtualCardAction);

    boolean isVirtualCardAction();

    String getPerformingPlayerId();

    String getText(DefaultGame game);

    Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException;
    SubAction createSubAction();

    boolean canBeInitiated(DefaultGame cardGame);

    default Action generateSnapshot(SnapshotData snapshotData) {
        return this;
    }
    boolean wasCarriedOut();
}