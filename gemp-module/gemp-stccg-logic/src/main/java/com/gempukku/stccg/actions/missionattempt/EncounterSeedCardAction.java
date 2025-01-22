package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.List;
import java.util.Objects;

public class EncounterSeedCardAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;
    private final AttemptMissionAction _parentAction;
    private enum Progress { effectsAdded }
    private final AttemptingUnit _attemptingUnit;

    public EncounterSeedCardAction(Player encounteringPlayer, PhysicalCard encounteredCard,
                                   AttemptingUnit attemptingUnit, AttemptMissionAction attemptAction)
            throws InvalidGameLogicException {
        super(encounteredCard.getGame(), encounteringPlayer, "Reveal seed card", ActionType.ENCOUNTER_SEED_CARD, Progress.values());
        try {
            _parentAction = Objects.requireNonNull(attemptAction);
            _cardTarget = new FixedCardResolver(encounteredCard);
            _attemptingUnit = Objects.requireNonNull(attemptingUnit);
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
        if (!getProgress(Progress.effectsAdded)) {
            PhysicalCard encounteredCard = getEncounteredCard();
            MissionLocation location = encounteredCard.getLocation();
            List<Action> encounterActions =
                    encounteredCard.getEncounterActions(cardGame, _attemptingUnit, this, location);
            for (Action action : encounterActions)
                appendEffect(action);
            setProgress(Progress.effectsAdded);
        }
        return getNextAction();
    }

    public AttemptingUnit getAttemptingUnit() throws InvalidGameLogicException { return _attemptingUnit; }
    public PhysicalCard getEncounteredCard() { return _cardTarget.getCard(); }

}