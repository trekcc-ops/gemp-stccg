package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="actionId")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Action {
    String getCardActionPrefix();

    int getActionId();
    void insertCost(Action costAction);
    void appendCost(Action costAction);
    void appendEffect(Action actionEffect);

    Action nextAction(DefaultGame game) throws InvalidGameLogicException, CardNotFoundException;

    enum ActionType {
        PLAY_CARD, SPECIAL_ABILITY, OTHER,
        MOVE_CARDS, ACTIVATE_TRIBBLE_POWER, ATTEMPT_MISSION,
        BATTLE, SELECT_CARD, SEED_CARD, ENCOUNTER_SEED_CARD, KILL, DISCARD, DRAW_CARD, REMOVE_CARD_FROM_PLAY,
        STOP_CARDS, SELECT_AWAY_TEAM, PLACE_CARD, FAIL_DILEMMA, DOWNLOAD_CARD, SELECT_ACTION, ADD_MODIFIER,
        USE_GAME_TEXT, USAGE_LIMIT, SCORE_POINTS, SELECT_AFFILIATION, SELECT_SKILL, MAKE_DECISION, OVERCOME_DILEMMA,
        REVEAL_SEED_CARD
    }

    ActionType getActionType();
    String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException;

    String getPerformingPlayerId();

    boolean canBeInitiated(DefaultGame cardGame);
    void setText(String text);
    boolean wasCarriedOut();

    void insertEffect(Action actionEffect);

}