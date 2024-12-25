package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collections;

public class DiscardCardAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private PhysicalCard _cardToDiscard;
    private SelectCardInPlayAction _selectAction;

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, SelectCardInPlayAction selectAction) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _selectAction = selectAction;
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, PhysicalCard cardToDiscard) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardToDiscard = cardToDiscard;
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
        if (_cardToDiscard == null) {
            if (_selectAction == null) {
                throw new InvalidGameLogicException("Unable to target a card to discard");
            } else {
                if (_selectAction.wasCarriedOut()) {
                    _cardToDiscard = _selectAction.getSelectedCard();
                } else {
                    return _selectAction;
                }
            }
        }

        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZone(_performingPlayerId, Collections.singleton(_cardToDiscard));
        if (_cardToDiscard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
            stCard.unstop();
        }
        gameState.addCardToZone(_cardToDiscard, Zone.DISCARD);
        cardGame.sendMessage(_performingPlayerId + " discards " + _cardToDiscard.getCardLink());
        cardGame.getActionsEnvironment().emitEffectResult(new DiscardCardFromPlayResult(_performingCard, _cardToDiscard));
        return getNextAction();
    }
}