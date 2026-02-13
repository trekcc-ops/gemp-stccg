package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collection;

public class SeedCardToDestinationResolver implements ActionTargetResolver {

    private PhysicalCard _cardEnteringPlay;
    private final Collection<PhysicalCard> _seedableCards;
    private final Collection<PhysicalCard> _destinationOptions;
    private SelectCardAction _selectCardToSeedAction;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private PhysicalCard _destinationCard;
    private boolean _isFailed;

    public SeedCardToDestinationResolver(String performingPlayerName, Collection<PhysicalCard> seedableCards,
                                         Collection<PhysicalCard> destinationOptions) {
        _seedableCards = seedableCards;
        _destinationOptions = destinationOptions;
        _performingPlayerName = performingPlayerName;
    }


    @Override
    public void resolve(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            if (_cardEnteringPlay == null) {
                selectCardToSeed(stGame);
            } else if (_destinationCard == null) {
                selectDestination(stGame);
            }
        } else {
            _isFailed = true;
        }
    }

    private void selectCardToSeed(ST1EGame stGame) {
        if (_selectCardToSeedAction == null) {
            _selectCardToSeedAction = new SelectVisibleCardAction(stGame, _performingPlayerName,
                    "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                    new InCardListFilter(_seedableCards));
            stGame.addActionToStack(_selectCardToSeedAction);
        } else if (_selectCardToSeedAction.wasSuccessful()) {
            _cardEnteringPlay = _selectCardToSeedAction.getSelectedCard();
        } else if (_selectCardToSeedAction.wasFailed()) {
            _isFailed = true;
        }
    }


    private void selectDestination(ST1EGame stGame) {
        if (_selectDestinationAction == null) {
            _selectDestinationAction = new SelectVisibleCardAction(stGame, _performingPlayerName,
                    "Choose a mission to seed " + _cardEnteringPlay.getCardLink() + " at",
                    new InCardListFilter(_destinationOptions));
            stGame.addActionToStack(_selectDestinationAction);
        } else if (_selectDestinationAction.wasSuccessful()) {
            _destinationCard = _selectDestinationAction.getSelectedCard();
        } else if (_selectDestinationAction.wasFailed()) {
            _isFailed = true;
        }
    }


    @Override
    public boolean isResolved() {
        return _cardEnteringPlay != null && _destinationCard != null;
    }

    @Override
    public boolean cannotBeResolved(DefaultGame cardGame) {
        return _isFailed || _seedableCards.isEmpty() || _destinationOptions.isEmpty();
    }

    public PhysicalCard getDestination() {
        return _destinationCard;
    }

    public PhysicalCard getCardToSeed() {
        return _cardEnteringPlay;
    }

}