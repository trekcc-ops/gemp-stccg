package com.gempukku.stccg.effects.discount;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoveCardsFromDiscardDiscountEffect implements DiscountEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;
    private final int _discount;
    private final Filterable _cardFilter;

    private boolean _paid;
    private boolean _required;
    private final ActionContext _actionContext;

    public RemoveCardsFromDiscardDiscountEffect(ActionContext actionContext, int count, int discount,
                                                Filterable cardFilter) {
        _source = actionContext.getSource();
        _playerId = actionContext.getPerformingPlayer();
        _count = count;
        _discount = discount;
        _cardFilter = cardFilter;
        _actionContext = actionContext;
    }

    @Override
    public int getDiscountPaidFor() {
        return _paid ? _discount : 0;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _required = (minimalDiscount > 0);
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
        return Filters.filter(
                _actionContext.getGameState().getDiscard(_playerId), _actionContext.getGame(), _cardFilter
        ).size() >= _count;
    }

    @Override
    public int getMaximumPossibleDiscount() {
        return Filters.filter(_actionContext.getGameState().getDiscard(_playerId), _actionContext.getGame(), _cardFilter).size();
    }

    @Override
    public void playEffect() {
        if (isPlayableInFull()) {
            if (!_required) {
                _actionContext.getGame().getUserFeedback().sendAwaitingDecision(_playerId,
                        new YesNoDecision("Do you want to remove cards from discard instead of paying twilight cost?") {
                            @Override
                            protected void yes() {
                                proceedDiscount(_actionContext.getGame());
                            }
                        });
            } else {
                proceedDiscount(_actionContext.getGame());
            }
        }
    }

    private void proceedDiscount(final DefaultGame game) {
        final Collection<PhysicalCard> removableCards = Filters.filter(game.getGameState().getDiscard(_playerId), game, _cardFilter);
        game.getUserFeedback().sendAwaitingDecision(_playerId,
                new ArbitraryCardsSelectionDecision(1, "Choose cards to remove", removableCards, _count, _count) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        removeCards(game, getSelectedCardsByResponse(result));
                        _paid = true;
                    }
                });
    }

    private void removeCards(DefaultGame game, List<PhysicalCard> cardsToRemove) {
        Set<PhysicalCard> removedCards = new HashSet<>();
        for (PhysicalCard physicalCard : cardsToRemove)
            if (physicalCard.getZone() == Zone.DISCARD)
                removedCards.add(physicalCard);

        game.getGameState().removeCardsFromZone(_playerId, removedCards);
        for (PhysicalCard removedCard : removedCards)
            game.getGameState().addCardToZone(removedCard, Zone.REMOVED);

        game.getGameState().sendMessage(_playerId + " removed " + GameUtils.getConcatenatedCardLinks(removedCards) + " from discard using " + _source.getCardLink());
        discountPaidCallback(removedCards.size());
    }

    @Override
    public boolean wasCarriedOut() {
        return !_required || _paid;
    }

    protected void discountPaidCallback(int paid) {  }
}
