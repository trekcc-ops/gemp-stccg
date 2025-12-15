package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class AttemptingUnitResolver implements ActionTargetResolver {

    private SelectAttemptingUnitAction _selectAction;
    private AttemptingUnit _attemptingUnit;
    private boolean _isFailed;

    public AttemptingUnitResolver(SelectAttemptingUnitAction selectionAction) {
        _selectAction = selectionAction;
    }

    public AttemptingUnitResolver(AttemptingUnit attemptingUnit) {
        _attemptingUnit = attemptingUnit;
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_selectAction.wasInitiated()) {
            cardGame.addActionToStack(_selectAction);
        } else if (_selectAction.wasSuccessful()) {
            _attemptingUnit = _selectAction.getSelection();
        } else if (_selectAction.wasFailed()) {
            _isFailed = true;
        }
    }

    public boolean isResolved() {
        return _attemptingUnit != null;
    }

    public AttemptingUnit getAttemptingUnit() {
        return _attemptingUnit;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return _isFailed;
    }

}