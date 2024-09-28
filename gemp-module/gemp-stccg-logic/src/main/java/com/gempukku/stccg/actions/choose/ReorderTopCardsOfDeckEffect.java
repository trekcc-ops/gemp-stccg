package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReorderTopCardsOfDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final String _deckId;
    private final int _count;

    public ReorderTopCardsOfDeckEffect(DefaultGame game, Action action, String playerId, String deckId, int count) {
        super(game);
        _action = action;
        _playerId = playerId;
        _deckId = deckId;
        _count = count;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getGameState().getDrawDeck(_deckId).size() >= _count;
    }

    @Override
    public void playEffect() {
        final List<? extends PhysicalCard> deck = _game.getGameState().getDrawDeck(_deckId);
        int count = Math.min(deck.size(), _count);
        Set<PhysicalCard> cards = new HashSet<>(deck.subList(0, count));

        _game.sendMessage(_playerId + " reorders top " + count + " cards of draw deck");

        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(
                new ChooseAndPutNextCardFromDeckOnTopOfDeck(subAction, cards));
        processSubAction(_game, subAction);
    }

    private class ChooseAndPutNextCardFromDeckOnTopOfDeck extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;

        public ChooseAndPutNextCardFromDeckOnTopOfDeck(CostToEffectAction subAction, Collection<PhysicalCard> remainingCards) {
            super(subAction.getGame(), _playerId, "Choose a card to put on top of the deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(new PutCardsFromZoneOnEndOfPileEffect(
                        _game, false, Zone.DRAW_DECK, Zone.DRAW_DECK, EndOfPile.TOP, selectedCard));
                _remainingCards.remove(selectedCard);
                if (!_remainingCards.isEmpty())
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromDeckOnTopOfDeck(_subAction, _remainingCards));
            }
        }
    }

}
