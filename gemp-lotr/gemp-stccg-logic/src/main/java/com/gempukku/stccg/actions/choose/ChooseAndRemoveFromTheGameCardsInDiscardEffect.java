package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class ChooseAndRemoveFromTheGameCardsInDiscardEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filterable[] _filters;
    private boolean _success;
    private final DefaultGame _game;

    public ChooseAndRemoveFromTheGameCardsInDiscardEffect(DefaultGame game, Action action, PhysicalCard source, String playerId, int minimum, int maximum, Filterable... filters) {
        _action = action;
        _source = source;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _filters = filters;
        _game = game;
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
        return Filters.filter(_game.getGameState().getDiscard(_playerId), _game, _filters).size() >= _minimum;
    }

    @Override
    public void playEffect() {
        final Collection<PhysicalCard> possibleTargets = Filters.filter(_game.getGameState().getDiscard(_playerId), _game, _filters);

        if (possibleTargets.size() <= _minimum) {
            processForCards(_game, possibleTargets);
        } else {
            int min = _minimum;
            int max = Math.min(_maximum, possibleTargets.size());
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose cards to remove from the game", possibleTargets, min, max) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            processForCards(_game, selectedCards);
                        }
                    });
        }
    }

    private void processForCards(DefaultGame game, Collection<PhysicalCard> cards) {
        CostToEffectAction _resultSubAction = _action.createSubAction();
        _resultSubAction.appendEffect(
                new RemoveCardsFromZoneEffect(game, _playerId, _source, cards, Zone.DISCARD));
        processSubAction(game, _resultSubAction);
        _success = cards.size() >= _minimum;
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _success;
    }
}
