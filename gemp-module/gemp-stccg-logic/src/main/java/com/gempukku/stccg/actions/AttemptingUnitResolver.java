package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class AttemptingUnitResolver {

    private SelectAttemptingUnitAction _selectAction;
    private AttemptingUnit _attemptingUnit;
    private boolean _resolved;

    public AttemptingUnitResolver(SelectAttemptingUnitAction selectionAction) {
        _selectAction = selectionAction;
    }

    public AttemptingUnitResolver(AttemptingUnit attemptingUnit) {
        _attemptingUnit = attemptingUnit;
        _resolved = true;
    }

    public void resolve() throws InvalidGameLogicException {
        if (!_resolved) {
            if (_selectAction != null && _selectAction.wasCarriedOut()) {
                _attemptingUnit = _selectAction.getSelection();
                _resolved = true;
            } else {
                throw new InvalidGameLogicException("Unable to resolve attempting unit");
            }
        }
    }

    public boolean isResolved() {
        return _resolved;
    }

    public AttemptingUnit getAttemptingUnit() throws InvalidGameLogicException {
        if (_resolved) {
            return _attemptingUnit;
        } else if (_selectAction != null && _selectAction.wasCarriedOut()) {
            return _selectAction.getSelection();
        } else {
            throw new InvalidGameLogicException("Unable to identify attempting unit from AttemptingUnitResolver");
        }
    }

    public SelectAttemptingUnitAction getSelectionAction() {
        return _selectAction;
    }

}