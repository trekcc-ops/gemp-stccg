package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;

public class PutCardsFromHandBeneathDrawDeckInChosenOrderEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final Player _player;
    private final Filterable[] _filters;

    private final boolean _reveal;

    public PutCardsFromHandBeneathDrawDeckInChosenOrderEffect(DefaultGame game,  Action action, String playerId,
                                                              boolean reveal, Filterable... filters) {
        super(game);
        _player = game.getPlayer(playerId);
        _action = action;
        _playerId = playerId;
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
        return true;
    }

    @Override
    public void playEffect() {
        final Collection<PhysicalCard> cards = Filters.filter(_game.getGameState().getHand(_playerId), _game, _filters);
        SubAction subAction = new SubAction(_action, _game);
        subAction.appendEffect(
                new ChooseAndPutNextCardFromHandOnBottomOfLibrary(subAction, cards));
        processSubAction(_game, subAction);
    }

    private class ChooseAndPutNextCardFromHandOnBottomOfLibrary extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;

        public ChooseAndPutNextCardFromHandOnBottomOfLibrary(CostToEffectAction subAction, Collection<PhysicalCard> remainingCards) {
            super(_player, "Choose a card to put on bottom of your deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardsFromZoneOnEndOfPileEffect(_game, _reveal, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, selectedCard));
                _remainingCards.remove(selectedCard);
                if (!_remainingCards.isEmpty())
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromHandOnBottomOfLibrary(_subAction, _remainingCards));
            }
        }
    }
}