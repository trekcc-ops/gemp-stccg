package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

@JsonIdentityInfo(scope=Action.class, generator= ObjectIdGenerators.PropertyGenerator.class, property="actionId")
@JsonIncludeProperties({ "actionId", "actionType", "performingPlayerId", "status", "targetCardId", "targetCardIds",
        "performingCardId", "pointsScored", "originCardId", "destinationCardId", "locationId" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Action {
    String getCardActionPrefix();
    void setCardActionPrefix(String prefix);

    @JsonProperty("actionId")
    int getActionId();
    void insertCost(Action costAction);
    void insertCosts(Collection<Action> actions);

    void appendCost(Action costAction);
    void appendEffect(Action actionEffect);

    Action nextAction(DefaultGame game) throws InvalidGameLogicException, CardNotFoundException, PlayerNotFoundException, InvalidGameOperationException;

    @JsonProperty("actionType")
    ActionType getActionType();

    @JsonProperty("performingPlayerId")
    String getPerformingPlayerId();

    boolean canBeInitiated(DefaultGame cardGame);

    boolean wasCarriedOut();

    void insertActions(Collection<Action> actions);

    void insertAction(Action action);

    void startPerforming() throws InvalidGameLogicException;

    boolean isInProgress();

    boolean wasCompleted();

    boolean wasFailed();
    void setAsFailed();

    void clearResult();
    ActionResult getResult();
    ActionContext getContext();

    default void appendExtraCostsFromModifiers(PhysicalCard target, DefaultGame cardGame) {
        for (Modifier modifier :
                cardGame.getModifiersAffectingCardByEffect(ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            modifier.appendExtraCosts(cardGame, this, target);
        }
    }

    @JsonProperty("actionId")
    void setActionId(int actionId);

}