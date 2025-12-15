package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
            if (!_selectAction.wasInitiated()) {
                cardGame.addActionToStack(_selectAction);
            } else if (_selectAction.wasSuccessful()) {
                _cards = _selectAction.getSelectedCards();
                _resolved = true;
            } else if (_selectAction.wasFailed()) {
                throw new InvalidGameLogicException("Unable to resolve cards");
            }
        }
    }

    public Collection<PhysicalCard> getCards() {
        return Objects.requireNonNullElseGet(_cards, ArrayList::new);
    }

    public boolean isResolved() { return _resolved; }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        if (_selectAction.wasSuccessful()) {
            return false;
        } else if (_selectAction.wasFailed()) {
            return true;
        } else {
            return !_selectAction.canBeInitiated(cardGame);
        }
    }


    @JsonProperty("serialized")
    private String serialize() {
        return "selectAction(" + _selectAction.getActionId() + ")";
    }

}