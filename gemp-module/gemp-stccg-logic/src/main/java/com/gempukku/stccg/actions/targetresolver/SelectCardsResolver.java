package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class SelectCardsResolver implements ActionCardResolver {
    private final SelectCardsAction _selectAction;
    private boolean _resolved;
    private final GameTextContext _context;
    private final String _saveToMemoryId;
    private Collection<PhysicalCard> _selectedCards;

    public SelectCardsResolver(SelectCardsAction selectAction) {
        _selectAction = selectAction;
        _resolved = false;
        _context = null;
        _saveToMemoryId = null;
    }

    public SelectCardsResolver(SelectCardsAction selectAction, GameTextContext context, String saveToMemoryId) {
        _selectAction = selectAction;
        _resolved = false;
        _context = context;
        _saveToMemoryId = saveToMemoryId;
    }

    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_resolved) {
            if (!_selectAction.wasInitiated()) {
                cardGame.addActionToStack(_selectAction);
            } else if (_selectAction.wasSuccessful()) {
                _selectedCards = _selectAction.getSelectedCards();
                _resolved = true;
                if (_context != null && _saveToMemoryId != null) {
                    _context.setCardMemory(_saveToMemoryId, _selectedCards);
                }
            } else if (_selectAction.wasFailed()) {
                throw new InvalidGameLogicException("Unable to resolve cards");
            }
        }
    }

    public Collection<PhysicalCard> getCards() {
        return Objects.requireNonNullElseGet(_selectedCards, ArrayList::new);
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

    public Collection<? extends PhysicalCard> getSelectableCards(DefaultGame cardGame) {
        return _selectAction.getSelectableCards(cardGame);
    }

    public void setSelectedCards(Collection<PhysicalCard> cardsToSelect) {
        _selectedCards = cardsToSelect;
        _resolved = true;
    }
}