package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface Action {
    String getCardActionPrefix();

    int getActionId();

    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, OTHER,
        MOVE_CARDS, ACTIVATE_TRIBBLE_POWER, ATTEMPT_MISSION,
        BATTLE, SELECT_CARD, SEED_CARD, ENCOUNTER_SEED_CARD, KILL, DISCARD, REVEAL_SEED_CARD
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
    void setText(String text);
    boolean wasCarriedOut();
    void insertCost(Effect effect);
    void appendCost(Effect effect);
    void insertEffect(Effect effect);
    void appendEffect(Effect effect);
}