package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class SelectCardsResolver implements ActionCardResolver {
    private final SelectCardsAction _selectAction;
    private boolean _resolved;
    private Collection<PhysicalCard> _cards;
    public SelectCardsResolver(SelectCardsAction selectAction) {
        _selectAction = selectAction;
        _resolved = false;
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_resolved) {
            if (_selectAction.wasCarriedOut()) {
                _cards = _selectAction.getSelectedCards();
                _resolved = true;
            } else {
                throw new InvalidGameLogicException("Unable to resolve cards");
            }
        }
    }

    public Collection<PhysicalCard> getCards(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_resolved) {
            return _cards;
        } else if (_selectAction.wasCarriedOut()) {
            return _selectAction.getSelectedCards();
        } else {
            throw new InvalidGameLogicException("Unable to identify cards from ActionCardResolver");
        }
    }

    public SelectCardsAction getSelectionAction() {
        return _selectAction;
    }

    public boolean isResolved() { return _resolved; }

    public boolean willProbablyBeEmpty(DefaultGame cardGame) {
        try {
            Collection<PhysicalCard> cards = getCards(cardGame);
            return cards.isEmpty();
        } catch(InvalidGameLogicException exp) {
            if (_selectAction != null)
                return !_selectAction.canBeInitiated(cardGame);
            else return false;
        }
    }


    @JsonProperty("serialized")
    private String serialize() {
        return "selectAction(" + _selectAction.getActionId() + ")";
    }

}