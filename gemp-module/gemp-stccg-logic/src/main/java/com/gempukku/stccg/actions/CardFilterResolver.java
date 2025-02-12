package com.gempukku.stccg.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class CardFilterResolver implements ActionCardResolver {

    private final CardFilter _cardFilter;
    private Collection<PhysicalCard> _cards;
    boolean _resolved;

    public CardFilterResolver(CardFilter cardFilter) {
        _cardFilter = cardFilter;
    }

    public void resolve(DefaultGame cardGame) {
        if (!_resolved) {
            _cards = Filters.filter(cardGame, _cardFilter);
            _resolved = true;
        }
    }

    public boolean isResolved() {
        return _resolved;
    }

    public Collection<PhysicalCard> getCards(DefaultGame cardGame) {
        for (PhysicalCard card : cardGame.getGameState().getAllCardsInGame()) {
            if (card.getZone() == null) {
                System.out.println(card);
            }
        }
        if (_resolved) {
            return _cards;
        } else {
            return Filters.filter(cardGame, _cardFilter);
        }
    }

    @Override
    public SelectCardsAction getSelectionAction() {
        return null;
    }

    public boolean willProbablyBeEmpty(DefaultGame cardGame) {
        return getCards(cardGame).isEmpty();
    }

    @JsonProperty("serialized")
    public String serialize() {
        return "filtered";
    }

}