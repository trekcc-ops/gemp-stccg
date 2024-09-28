package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardsFromHandBeneathDrawDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;
    private final Filterable[] _filters;

    private final boolean _reveal;

    public ChooseAndPutCardsFromHandBeneathDrawDeckEffect(DefaultGame game, Action action, String playerId, int count, boolean reveal, Filterable... filters) {
        super(game);
        _action = action;
        _playerId = playerId;
        _count = count;
        _filters = filters;
        _reveal = reveal;
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
        return Filters.filter(_game.getGameState().getHand(_playerId), _game, _filters).size() >= _count;
    }

    @Override
    public void playEffect() {
        final Collection<PhysicalCard> cards = Filters.filter(_game.getGameState().getHand(_playerId), _game, _filters);
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(
                new ChooseAndPutNextCardFromHandOnBottomOfLibrary(subAction, Math.min(_count, cards.size()), cards));
        processSubAction(_game, subAction);
    }

    private class ChooseAndPutNextCardFromHandOnBottomOfLibrary extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;
        private final int _remainingCount;

        public ChooseAndPutNextCardFromHandOnBottomOfLibrary(CostToEffectAction subAction, int remainingCount, Collection<PhysicalCard> remainingCards) {
            super(subAction.getGame(), _playerId, "Choose a card to put on bottom of your deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCount = remainingCount;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardsFromZoneOnEndOfPileEffect(_game, _reveal,  Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, selectedCard));
                _remainingCards.remove(selectedCard);
                if (_remainingCount - 1 > 0)
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromHandOnBottomOfLibrary(_subAction, _remainingCount - 1, _remainingCards));
            }
        }
    }
}
