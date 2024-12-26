package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;
import java.util.Collections;

public class DiscardCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private SelectCardInPlayAction _selectAction;
    private Filter _cardFilter;
    private Collection<PhysicalCard> _cardsToDiscard;

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, SelectCardInPlayAction selectAction) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _selectAction = selectAction;
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, PhysicalCard cardToDiscard) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardsToDiscard = Collections.singleton(cardToDiscard);
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer,
                             Collection<PhysicalCard> cardsToDiscard) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardsToDiscard = cardsToDiscard;
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, Filter cardFilter) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardFilter = cardFilter;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (_cardsToDiscard == null) {
            if (_selectAction == null) {
                if (_cardFilter == null) {
                    throw new InvalidGameLogicException("Unable to target a card to discard");
                } else {
                    _cardsToDiscard = Filters.filter(cardGame, _cardFilter);
                }
            } else {
                if (_selectAction.wasCarriedOut()) {
                    _cardsToDiscard = _selectAction.getSelectedCards();
                } else {
                    return _selectAction;
                }
            }
        }

        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZone(_performingPlayerId, _cardsToDiscard);
        for (PhysicalCard cardToDiscard : _cardsToDiscard) {
            if (cardToDiscard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
            gameState.addCardToZone(cardToDiscard, Zone.DISCARD);
            cardGame.getActionsEnvironment().emitEffectResult(new DiscardCardFromPlayResult(_performingCard, cardToDiscard));
        }
        cardGame.sendMessage(_performingPlayerId + " discards " + TextUtils.getConcatenatedCardLinks(_cardsToDiscard));
        return getNextAction();
    }
}