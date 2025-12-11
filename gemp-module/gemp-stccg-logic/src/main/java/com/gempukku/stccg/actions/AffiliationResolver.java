package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class AffiliationResolver {
    private SelectAffiliationAction _selectAction;
    private Affiliation _affiliation;
    private boolean _resolved;

    public AffiliationResolver(SelectAffiliationAction selectionAction) {
        _selectAction = selectionAction;
    }

    public AffiliationResolver(Affiliation affiliation) {
        _affiliation = affiliation;
        _resolved = true;
    }

    public void resolve() throws InvalidGameLogicException {
        if (!_resolved) {
            if (_selectAction != null && _selectAction.wasCarriedOut()) {
                _affiliation = _selectAction.getSelectedAffiliation();
                _resolved = true;
            } else {
                throw new InvalidGameLogicException("Unable to resolve attempting unit");
            }
        }
    }

    public boolean isResolved() {
        return _resolved;
    }

    public Affiliation getAffiliation() throws InvalidGameLogicException {
        if (_resolved) {
            return _affiliation;
        } else if (_selectAction != null && _selectAction.wasCarriedOut()) {
            return _selectAction.getSelectedAffiliation();
        } else {
            throw new InvalidGameLogicException("Unable to identify attempting unit from AttemptingUnitResolver");
        }
    }

    public SelectAffiliationAction getSelectionAction() {
        return _selectAction;
    }

}