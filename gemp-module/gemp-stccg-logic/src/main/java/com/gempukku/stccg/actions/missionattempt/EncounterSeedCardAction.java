package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;
import java.util.Objects;

public class EncounterSeedCardAction extends ActionyAction implements TopLevelSelectableAction {
    private final FixedCardResolver _cardTarget;
    private final AttemptMissionAction _parentAction;

    private enum Progress { effectsAdded }
    private final AttemptingUnit _attemptingUnit;
    private final MissionLocation _missionLocation;

    public EncounterSeedCardAction(DefaultGame cardGame, Player encounteringPlayer, PhysicalCard encounteredCard,
                                   AttemptingUnit attemptingUnit, AttemptMissionAction attemptAction,
                                   MissionLocation location)
            throws InvalidGameLogicException {
        super(cardGame, encounteringPlayer, "Reveal seed card", ActionType.ENCOUNTER_SEED_CARD, Progress.values());
        try {
            _parentAction = Objects.requireNonNull(attemptAction);
            _cardTarget = new FixedCardResolver(encounteredCard);
            _attemptingUnit = Objects.requireNonNull(attemptingUnit);
            _missionLocation = Objects.requireNonNull(location);
        } catch(NullPointerException npe) {
            throw new InvalidGameLogicException(npe.getMessage());
        }
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (isBeingInitiated())
            setAsInitiated();
                // TODO - handling if one effect of the dilemma makes it impossible to perform another
/*        if (_attemptingUnit.getAttemptingPersonnel().isEmpty())
            setAsFailed(); */
        Action nextAction = getNextAction();
        if (nextAction == null) {
            if (!wasFailed())
                setAsSuccessful();
        }
        return nextAction;
    }

    public AttemptingUnit getAttemptingUnit() throws InvalidGameLogicException { return _attemptingUnit; }
    public PhysicalCard getEncounteredCard() { return _cardTarget.getCard(); }
    public AttemptMissionAction getAttemptAction() { return _parentAction; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _cardTarget.getCard();
    }

    @Override
    public int getCardIdForActionSelection() {
        return _cardTarget.getCard().getCardId();
    }


}