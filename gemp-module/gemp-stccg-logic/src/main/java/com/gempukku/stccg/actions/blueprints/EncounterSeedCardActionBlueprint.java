package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class EncounterSeedCardActionBlueprint extends DefaultActionBlueprint {

    private final List<SubActionBlueprint> _effects;

    public EncounterSeedCardActionBlueprint(@JsonProperty("effect")
                                            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                            List<SubActionBlueprint> effects) {
        super("Encounter card", 0, Phase.EXECUTE_ORDERS);
        _effects = Objects.requireNonNullElse(effects, new LinkedList<>());
    }

    public EncounterSeedCardAction createAction(DefaultGame cardGame, Player performingPlayer, PhysicalCard thisCard,
                                                 AttemptingUnit attemptingUnit, MissionLocation missionLocation,
                                                 AttemptMissionAction missionAttemptAction)
            throws InvalidGameLogicException {
        ActionContext actionContext = new DefaultActionContext(cardGame, thisCard, performingPlayer);
        EncounterSeedCardAction encounterAction =
                new EncounterSeedCardAction(cardGame, performingPlayer, thisCard, attemptingUnit, missionAttemptAction,
                        missionLocation);
        _effects.forEach(actionEffect -> actionEffect.addEffectToAction(false, encounterAction, actionContext));
        return encounterAction;
    }

    @Override
    protected EncounterSeedCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext context) {
        try {
            Stack<Action> actionStack = context.getGame().getActionsEnvironment().getActionStack();
            for (Action action : actionStack) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getLocation() == card.getGameLocation()) {
                    Player performingPlayer = context.getPerformingPlayer();
                    DefaultGame cardGame = context.getGame();
                    EncounterSeedCardAction encounterAction = new EncounterSeedCardAction(cardGame, performingPlayer,
                            card, attemptAction.getAttemptingUnit(), attemptAction, attemptAction.getLocation());
                    appendActionToContext(encounterAction, context);
                    return encounterAction;
                }
            }
            throw new InvalidGameLogicException("Could not identify an active mission attempt for this encounter");
        } catch(InvalidGameLogicException exp) {
            context.getGame().sendErrorMessage(exp);
            return null;
        }
    }
}