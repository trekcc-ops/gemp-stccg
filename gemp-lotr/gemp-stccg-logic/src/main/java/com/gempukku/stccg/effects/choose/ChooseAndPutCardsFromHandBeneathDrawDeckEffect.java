package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class ChooseAndPutCardsFromHandBeneathDrawDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;
    private final Filterable[] _filters;

    private final boolean _reveal;

    public ChooseAndPutCardsFromHandBeneathDrawDeckEffect(Action action, String playerId, int count, boolean reveal, Filterable... filters) {
        _action = action;
        _playerId = playerId;
        _count = count;
        _filters = filters;
        _reveal = reveal;
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filters).size() >= _count;
    }

    @Override
    public void playEffect(DefaultGame game) {
        final Collection<PhysicalCard> cards = Filters.filter(game.getGameState().getHand(_playerId), game, _filters);
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new ChooseAndPutNextCardFromHandOnBottomOfLibrary(subAction, Math.min(_count, cards.size()), cards));
        processSubAction(game, subAction);
    }

    private class ChooseAndPutNextCardFromHandOnBottomOfLibrary extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;
        private final int _remainingCount;

        public ChooseAndPutNextCardFromHandOnBottomOfLibrary(CostToEffectAction subAction, int remainingCount, Collection<PhysicalCard> remainingCards) {
            super(_playerId, "Choose a card to put on bottom of your deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCount = remainingCount;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardsFromZoneOnEndOfPileEffect(_reveal,  Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, selectedCard));
                _remainingCards.remove(selectedCard);
                if (_remainingCount - 1 > 0)
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromHandOnBottomOfLibrary(_subAction, _remainingCount - 1, _remainingCards));
            }
        }
    }
}
