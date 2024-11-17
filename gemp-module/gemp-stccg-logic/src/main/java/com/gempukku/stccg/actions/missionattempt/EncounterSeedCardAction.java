package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class EncounterSeedCardAction extends ActionyAction {
    private final PhysicalCard _encounteredCard;
    private final MissionCard _mission;
    private final AttemptingUnit _attemptingUnit;
    private boolean _effectsAdded;
    private final AttemptMissionAction _missionAttempt;

    public EncounterSeedCardAction(AttemptMissionAction missionAttempt, Player encounteringPlayer,
                                   PhysicalCard encounteredCard, MissionCard mission, AttemptingUnit attemptingUnit) {
        super(encounteringPlayer, "Reveal seed card", ActionType.ENCOUNTER_SEED_CARD);
        _encounteredCard = encounteredCard;
        _mission = mission;
        _attemptingUnit = attemptingUnit;
        _missionAttempt = missionAttempt;
    }


    @Override
    public PhysicalCard getActionSource() {
        return _encounteredCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _encounteredCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_effectsAdded) {
            List<Action> encounterActions =
                    _encounteredCard.getEncounterActions(cardGame, _attemptingUnit, _mission, this);
            for (Action action : encounterActions)
                appendAction(action);
            _effectsAdded = true;
        }
        return getNextAction();
    }

    public AttemptMissionAction getMissionAttempt() { return _missionAttempt; }
}