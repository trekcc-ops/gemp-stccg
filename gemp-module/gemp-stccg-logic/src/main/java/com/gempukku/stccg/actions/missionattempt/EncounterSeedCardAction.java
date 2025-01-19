package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;

public class EncounterSeedCardAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;
    private final AttemptMissionAction _parentAction;
    private enum Progress { effectsAdded }

    public EncounterSeedCardAction(Player encounteringPlayer, PhysicalCard encounteredCard,
                                   AttemptMissionAction attemptAction) throws InvalidGameLogicException {
        super(encounteringPlayer, "Reveal seed card", ActionType.ENCOUNTER_SEED_CARD, Progress.values());
        _parentAction = attemptAction;
        _cardTarget = new FixedCardResolver(encounteredCard);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!getProgress(Progress.effectsAdded)) {
            PhysicalCard encounteredCard = getEncounteredCard();
            AttemptingUnit attemptingUnit = _parentAction.getAttemptingUnit();
            MissionLocation location = encounteredCard.getLocation();
            List<Action> encounterActions =
                    encounteredCard.getEncounterActions(cardGame, attemptingUnit, this, location);
            for (Action action : encounterActions)
                appendEffect(action);
            setProgress(Progress.effectsAdded);
        }
        return getNextAction();
    }

    public AttemptingUnit getAttemptingUnit() { return _parentAction.getAttemptingUnit(); }
    public PhysicalCard getEncounteredCard() { return _cardTarget.getCard(); }

}