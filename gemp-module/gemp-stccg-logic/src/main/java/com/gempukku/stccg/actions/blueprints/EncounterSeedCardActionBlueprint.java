package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.*;

public class EncounterSeedCardActionBlueprint extends DefaultActionBlueprint {


    public EncounterSeedCardActionBlueprint(@JsonProperty("effect")
                                            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                            List<SubActionBlueprint> effects) throws InvalidCardDefinitionException {
        super(0, new ArrayList<>(), Objects.requireNonNullElse(effects, new LinkedList<>()),
                new YouPlayerSource());
    }

    public EncounterSeedCardAction createAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard thisCard,
                                                AttemptingUnit attemptingUnit, MissionLocation missionLocation,
                                                AttemptMissionAction missionAttemptAction)
            throws InvalidGameLogicException {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        EncounterSeedCardAction encounterAction =
                new EncounterSeedCardAction(cardGame, performingPlayerName, thisCard, attemptingUnit, missionAttemptAction,
                        missionLocation.getLocationId(), actionContext);
        effects.forEach(actionEffect -> actionEffect.addEffectToAction(cardGame, false, encounterAction, actionContext));
        return encounterAction;
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard card) {
        try {
            ActionContext context = new ActionContext(card, performingPlayerName);
            Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
            for (Action action : actionStack) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getLocationId() == card.getLocationId()) {
                    TopLevelSelectableAction newAction = new EncounterSeedCardAction(cardGame, performingPlayerName,
                            card, attemptAction.getAttemptingUnit(), attemptAction, attemptAction.getLocationId(),
                            context);
                    appendActionToContext(cardGame, newAction, context);
                    return newAction;
                }
            }
            throw new InvalidGameLogicException("Could not identify an active mission attempt for this encounter");
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return null;
        }
    }


}