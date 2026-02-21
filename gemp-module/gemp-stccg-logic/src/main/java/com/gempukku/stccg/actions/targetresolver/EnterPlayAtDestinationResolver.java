package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.InCardListFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class EnterPlayAtDestinationResolver implements ActionTargetResolver {

    private PhysicalCard _cardEnteringPlay;
    private SelectCardAction _selectCardToSeedAction;
    private final String _performingPlayerName;
    private SelectCardAction _selectDestinationAction;
    private PhysicalCard _destinationCard;
    private boolean _isFailed;
    private final Map<PhysicalCard, Collection<PhysicalCard>> _destinationMap;

    public EnterPlayAtDestinationResolver(String performingPlayerName,
                                          Map<PhysicalCard, Collection<PhysicalCard>> destinationMap) {
        _performingPlayerName = performingPlayerName;
        _destinationMap = destinationMap;
    }

    public EnterPlayAtDestinationResolver(String performingPlayerName, Collection<PhysicalCard> seedableCards,
                                          Collection<PhysicalCard> destinationOptions) {
        _destinationMap = new HashMap<>();
        if (!destinationOptions.isEmpty()) {
            for (PhysicalCard card : seedableCards) {
                _destinationMap.put(card, destinationOptions);
            }
        }
        _performingPlayerName = performingPlayerName;
    }

    public EnterPlayAtDestinationResolver(String performingPlayerName, PhysicalCard cardToSeed,
                                          Collection<PhysicalCard> destinationOptions) {
        this(performingPlayerName, List.of(cardToSeed), destinationOptions);
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
                    "Choose a card to enter play",
                    new InCardListFilter(_destinationMap.keySet()));
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
                    "Choose a destination",
                    new InCardListFilter(_destinationMap.get(_cardEnteringPlay)));
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
        return _isFailed || _destinationMap.isEmpty();
    }

    public PhysicalCard getDestination() {
        return _destinationCard;
    }

    public PhysicalCard getCardEnteringPlay() {
        return _cardEnteringPlay;
    }

    public Collection<PhysicalCard> getSelectableOptions() { return _destinationMap.keySet(); }

    public void setCardEnteringPlay(PhysicalCard cardEnteringPlay) {
        _cardEnteringPlay = cardEnteringPlay;
    }

}