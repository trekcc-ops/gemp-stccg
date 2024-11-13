package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;

public class StopPersonnelAction extends ActionyAction {
    private final Collection<PersonnelCard> _personnelToStop;

    public StopPersonnelAction(Player performingPlayer, Collection<PersonnelCard>personnelToStop) {
        super(performingPlayer, "Stop personnel", ActionType.STOP_PERSONNEL);
        _personnelToStop = personnelToStop;
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return null;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
//        for (PersonnelCard personnel : _personnelToStop)
//            personnel.stop();
        return getNextAction();
    }
}