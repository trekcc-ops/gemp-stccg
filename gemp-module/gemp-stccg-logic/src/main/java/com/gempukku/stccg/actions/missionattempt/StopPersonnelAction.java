package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsInPlayAction;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;

public class StopPersonnelAction extends ActionyAction {
    private final Collection<PersonnelCard> _personnelToStop = new LinkedList<>();
    private final SelectCardsInPlayAction _selectionAction;
    private boolean _personnelChosen;

    public StopPersonnelAction(Player performingPlayer, Collection<? extends PersonnelCard> personnelToStop) {
        super(performingPlayer, "Stop personnel", ActionType.STOP_PERSONNEL);
        _personnelChosen = true;
        _personnelToStop.addAll(personnelToStop);
        _selectionAction = new SelectCardsInPlayAction(this, performingPlayer, "", personnelToStop,
                personnelToStop.size());
    }

    public StopPersonnelAction(Player performingPlayer, SelectCardsInPlayAction selectionAction) {
        super(performingPlayer, "Stop personnel", ActionType.STOP_PERSONNEL);
        _selectionAction = selectionAction;
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
    public String getActionSelectionText(DefaultGame game) {
        if (_personnelChosen && _personnelToStop.size() == 1) {
            return "Stop " + Iterables.getOnlyElement(_personnelToStop).getTitle();
        } else {
            return "Stop personnel";
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_personnelChosen) {
            if (!_selectionAction.wasCarriedOut()) {
                return _selectionAction;
            } else {
//                _personnelToStop.addAll(_selectionAction.getSelectedCards());
                _personnelChosen = true;
            }
        }

        for (PersonnelCard personnel : _personnelToStop) {
//            cardGame.getModifiersEnvironment().addUntilEndOf
        }
//            personnel.stop();
        return getNextAction();
    }
}