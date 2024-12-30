package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;

public class EncounterSeedCardAction extends ActionyAction {
    private final PhysicalCard _encounteredCard;
    private final MissionLocation _location;
    private final AttemptingUnit _attemptingUnit;
    private enum Progress { effectsAdded }

    public EncounterSeedCardAction(Player encounteringPlayer, PhysicalCard encounteredCard, MissionLocation mission,
                                   AttemptingUnit attemptingUnit) throws InvalidGameLogicException {
        super(encounteringPlayer, "Reveal seed card", ActionType.ENCOUNTER_SEED_CARD, Progress.values());
        _encounteredCard = encounteredCard;
        _location = mission;
        _attemptingUnit = attemptingUnit;
    }



    @Override
    public PhysicalCard getPerformingCard() {
        return _encounteredCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!getProgress(Progress.effectsAdded)) {
            List<Action> encounterActions = _encounteredCard.getEncounterActions(
                    cardGame, _attemptingUnit, this, _location);
            for (Action action : encounterActions)
                appendEffect(action);
            setProgress(Progress.effectsAdded);
        }
        return getNextAction();
    }

    public AttemptingUnit getAttemptingUnit() { return _attemptingUnit; }
    public PhysicalCard getEncounteredCard() { return _encounteredCard; }

    public MissionLocation getLocation() { return _location; }
}