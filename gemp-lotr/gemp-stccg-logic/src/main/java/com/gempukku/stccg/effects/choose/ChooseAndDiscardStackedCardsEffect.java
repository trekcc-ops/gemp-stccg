package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChooseAndDiscardStackedCardsEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filterable _stackedOnFilter;
    private final Filterable _stackedCardFilter;

    public ChooseAndDiscardStackedCardsEffect(Action action, String playerId, int minimum, int maximum, Filterable stackedOnFilter, Filterable stackedCardFilter) {
        _action = action;
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _stackedOnFilter = stackedOnFilter;
        _stackedCardFilter = stackedCardFilter;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Discard stacked card";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.countActive(game, _stackedOnFilter, Filters.hasStacked(_stackedCardFilter)) > 0;
    }

    @Override
    public void playEffect(final DefaultGame game) {
        List<PhysicalCard> discardableCards = new LinkedList<>();

        for (PhysicalCard stackedOnCard : Filters.filterActive(game, _stackedOnFilter))
            discardableCards.addAll(Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, _stackedCardFilter));

        if (discardableCards.size() <= _minimum) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new DiscardCardsFromZoneEffect(_action.getActionSource(), Zone.STACKED, discardableCards));
            discardingCardsCallback(discardableCards);
            processSubAction(game, subAction);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose cards to discard", discardableCards, _minimum, _maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            SubAction subAction = new SubAction(_action);
                            subAction.appendEffect(new DiscardCardsFromZoneEffect(_action.getActionSource(), Zone.STACKED, selectedCards));
                            discardingCardsCallback(selectedCards);
                            processSubAction(game, subAction);
                        }
                    });
        }
    }

    protected void discardingCardsCallback(Collection<PhysicalCard> cards) {

    }
}
