package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;

public class ShuffleCardsIntoDrawDeckAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    private final Filter _cardFilter;

    public ShuffleCardsIntoDrawDeckAction(PhysicalCard performingCard, Player performingPlayer,
                                          Filter cardFilter) {
        super(performingPlayer, "Shuffle cards into draw deck", ActionType.PLACE_CARD);
        _performingCard = performingCard;
        _cardFilter = cardFilter;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        Collection<PhysicalCard> cards = Filters.filter(cardGame.getGameState().getAllCardsInGame(), _cardFilter);
        return !cards.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Collection<PhysicalCard> cards = Filters.filter(cardGame.getGameState().getAllCardsInGame(), _cardFilter);
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