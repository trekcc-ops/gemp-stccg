package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "className")
@JsonIgnoreProperties(value = { "actionType" }, allowGetters = true)
@JsonIdentityInfo(scope=Action.class, generator= ObjectIdGenerators.PropertyGenerator.class, property="actionId")
@JsonIncludeProperties({ "actionId", "actionType", "performingPlayerId", "status", "targetCardId", "targetCardIds",
        "performingCardId", "pointsScored", "originCardId", "destinationCardId", "locationId", "destinationZone",
        "selectedIndex", "selectionOptions" })
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

    Action nextAction(DefaultGame game)
            throws InvalidGameLogicException, CardNotFoundException, PlayerNotFoundException,
            InvalidGameOperationException;

    @JsonProperty("actionType")
    ActionType getActionType();

    @JsonProperty("performingPlayerId")
    String getPerformingPlayerId();

    boolean canBeInitiated(DefaultGame cardGame);
    boolean wasInitiated();

    boolean wasCarriedOut();

    void insertActions(Collection<Action> actions);

    void insertAction(Action action);

    void startPerforming();

    boolean isInProgress();

    boolean wasCompleted();

    boolean wasFailed();
    void setAsFailed();

    void clearResult();
    ActionResult getResult();

    default void appendExtraCostsFromModifiers(PhysicalCard target, DefaultGame cardGame) {
        for (Modifier modifier :
                cardGame.getModifiersAffectingCardByEffect(ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            modifier.appendExtraCosts(cardGame, this, target);
        }
    }

    @JsonProperty("actionId")
    void setActionId(int actionId);

    boolean wasSuccessful();

    void executeNextSubAction(ActionsEnvironment actionsEnvironment, DefaultGame cardGame)
            throws PlayerNotFoundException, InvalidGameLogicException, InvalidGameOperationException,
            CardNotFoundException;

    void cancel();
}