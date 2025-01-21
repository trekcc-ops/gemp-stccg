package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    public ShuffleCardsIntoDrawDeckAction(PhysicalCard performingCard, Player performingPlayer,
                                          Filter cardFilter) {
        super(performingPlayer, "Shuffle cards into draw deck", ActionType.PLACE_CARD);
        _cardTarget = new CardFilterResolver(cardFilter);
        _performingCard = performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardTarget.willProbablyBeEmpty(cardGame);
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

        Collection<PhysicalCard> cards = _cardTarget.getCards(cardGame);
        cardGame.getGameState().removeCardsFromZone(_performingCard.getOwnerName(), cards);
        cardGame.getGameState().shuffleCardsIntoDeck(cards, _performingPlayerId);
        cardGame.sendMessage(TextUtils.concatenateStrings(
                cards.stream().map(PhysicalCard::getCardLink)) + " " +
                TextUtils.be(cards) + " shuffled into " + _performingPlayerId + " deck");
        return getNextAction();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }
}