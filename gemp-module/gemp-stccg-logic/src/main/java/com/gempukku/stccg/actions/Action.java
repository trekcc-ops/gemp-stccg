package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface Action {
    String getCardActionPrefix();

    int getActionId();

    void insertCost(DefaultGame cardGame, Action action);

    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, OTHER,
        MOVE_CARDS, ACTIVATE_TRIBBLE_POWER, ATTEMPT_MISSION,
        BATTLE, SELECT_CARD, SEED_CARD, ENCOUNTER_SEED_CARD, KILL, DISCARD, DRAW_CARD, REMOVE_CARD_FROM_PLAY, STOP_CARDS, SELECT_AWAY_TEAM, PLACE_CARD, FAIL_DILEMMA, DOWNLOAD_CARD, SELECT_ACTION, ADD_MODIFIER, USE_GAME_TEXT, USAGE_LIMIT, SCORE_POINTS, SELECT_AFFILIATION, SELECT_SKILL, MAKE_DECISION, REVEAL_SEED_CARD
    }

    ActionType getActionType();
    PhysicalCard getPerformingCard();
    String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException;
    PhysicalCard getCardForActionSelection();
    void setVirtualCardAction(boolean virtualCardAction);
    boolean isVirtualCardAction();
    String getPerformingPlayerId();
    Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException;

    boolean canBeInitiated(DefaultGame cardGame);
    void setText(String text);
    boolean wasCarriedOut();

    void appendCost(DefaultGame cardGame, Action costAction);

    void appendEffect(DefaultGame cardGame, Action actionEffect);
    void insertEffect(DefaultGame cardGame, Action actionEffect);
}