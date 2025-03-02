package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;

@JsonIdentityInfo(scope=Action.class, generator= ObjectIdGenerators.PropertyGenerator.class, property="actionId")
@JsonIncludeProperties({ "actionId", "actionType", "performingPlayerId", "status", "targetCardId", "targetCardIds",
        "performingCardId", "pointsScored", "originCardId", "destinationCardId" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Action {
    String getCardActionPrefix();

    int getActionId();
    void insertCost(Action costAction);
    void appendCost(Action costAction);
    void appendEffect(Action actionEffect);

    Action nextAction(DefaultGame game) throws InvalidGameLogicException, CardNotFoundException, PlayerNotFoundException, InvalidGameOperationException;

    @JsonProperty("actionType")
    ActionType getActionType();
    String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException;

    @JsonProperty("performingPlayerId")
    String getPerformingPlayerId();

    boolean canBeInitiated(DefaultGame cardGame);
    void setText(String text);
    boolean wasCarriedOut();

    void insertEffect(Action actionEffect);

    void startPerforming() throws InvalidGameLogicException;

    boolean isInProgress();

    boolean wasCompleted();

    boolean wasFailed();
    void setAsFailed();

}