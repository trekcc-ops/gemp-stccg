package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "className")
@JsonIgnoreProperties(value = { "actionType" }, allowGetters = true)
@JsonIdentityInfo(scope=Action.class, generator= ObjectIdGenerators.PropertyGenerator.class, property="actionId")
@JsonIncludeProperties({ "actionId", "actionType", "performingPlayerId", "seededCardId", "status", "targetCardId",
        "targetCardIds", "performingCardId", "pointsScored", "originCardId", "destinationCardId", "locationId",
        "destinationZone", "selectedIndex", "selectionOptions", "destination" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Action {
    String getCardActionPrefix();
    void setCardActionPrefix(String prefix);

    @JsonProperty("actionId")
    int getActionId();

    void insertCosts(Collection<Action> actions);

    void appendCost(Action costAction);

    @JsonProperty("actionType")
    ActionType getActionType();

    @JsonProperty("performingPlayerId")
    String getPerformingPlayerId();

    boolean canBeInitiated(DefaultGame cardGame);
    boolean wasInitiated();

    void startPerforming();

    boolean wasCompleted();

    boolean wasFailed();
    void setAsFailed();

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
            throws InvalidGameOperationException;

    void cancel();

    void setAsInitiated();

    boolean hasOncePerGameLimit();
}