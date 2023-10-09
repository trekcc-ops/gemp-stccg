package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.RemoveCardsFromZoneEffect;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.actions.Action;

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

    public ChooseAndRemoveFromTheGameCardsInDiscardEffect(Action action, PhysicalCard source, String playerId, int minimum, int maximum, Filterable... filters) {
        _action = action;
        _source = source;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _filters = filters;
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
        return Filters.filter(game.getGameState().getDiscard(_playerId), game, _filters).size() >= _minimum;
    }

    @Override
    public void playEffect(final DefaultGame game) {
        final Collection<PhysicalCard> possibleTargets = Filters.filter(game.getGameState().getDiscard(_playerId), game, _filters);

        if (possibleTargets.size() <= _minimum) {
            processForCards(game, possibleTargets);
        } else {
            int min = _minimum;
            int max = Math.min(_maximum, possibleTargets.size());
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose cards to remove from the game", possibleTargets, min, max) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            processForCards(game, selectedCards);
                        }
                    });
        }
    }

    private void processForCards(DefaultGame game, Collection<PhysicalCard> cards) {
        CostToEffectAction _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(
                new RemoveCardsFromZoneEffect(_playerId, _source, cards, Zone.DISCARD));
        processSubAction(game, _resultSubAction);
        _success = cards.size() >= _minimum;
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _success;
    }
}
