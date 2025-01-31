package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.turn.ActivateCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class EncounterSeedCardActionBlueprint {

    private final List<SubActionBlueprint> _effects;

    public EncounterSeedCardActionBlueprint(@JsonProperty("effect")
                                            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                            List<SubActionBlueprint> effects) {
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

}