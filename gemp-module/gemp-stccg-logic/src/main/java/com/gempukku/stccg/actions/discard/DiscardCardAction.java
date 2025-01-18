package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;

public class DiscardCardAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, SelectVisibleCardAction selectAction) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _performingCard = performingCard;
        _cardTarget = new ActionCardResolver(selectAction);
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, PhysicalCard cardToDiscard) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new ActionCardResolver(cardToDiscard);
        _performingCard = performingCard;
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer,
                             Collection<PhysicalCard> cardsToDiscard) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new ActionCardResolver(cardsToDiscard);
        _performingCard = performingCard;
    }

    public DiscardCardAction(PhysicalCard performingCard, Player performingPlayer, Filter cardFilter) {
        super(performingPlayer, "Discard", ActionType.DISCARD);
        _cardTarget = new ActionCardResolver(cardFilter);
        _performingCard = performingCard;
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        Collection<PhysicalCard> cardsToDiscard = _cardTarget.getCards(cardGame);
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZone(_performingPlayerId, cardsToDiscard);
        for (PhysicalCard cardToDiscard : cardsToDiscard) {
            if (cardToDiscard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
            gameState.addCardToZone(cardToDiscard, Zone.DISCARD);
            cardGame.getActionsEnvironment().emitEffectResult(
                    new DiscardCardFromPlayResult(_performingCard, cardToDiscard));
        }
        cardGame.sendMessage(_performingPlayerId + " discards " + TextUtils.getConcatenatedCardLinks(cardsToDiscard));
        return getNextAction();
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }
}