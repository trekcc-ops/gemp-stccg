package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.AbstractSubActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.TextUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealAndChooseCardsFromOpponentHandEffect extends AbstractSubActionEffect {
    private final String _playerId;
    private final String _opponentId;
    private final PhysicalCard _source;
    private final String _text;
    private final Filterable _selectionFilter;
    private final int _minChosen;
    private final int _maxChosen;

    protected RevealAndChooseCardsFromOpponentHandEffect(ActionContext actionContext, String playerId,
                                                         String opponentId, PhysicalCard source, String text,
                                                         Filterable selectionFilter, int minChosen, int maxChosen) {
        super(actionContext.getGame());
        _playerId = playerId;
        _opponentId = opponentId;
        _source = source;
        _text = text;
        _selectionFilter = selectionFilter;
        _minChosen = minChosen;
        _maxChosen = maxChosen;
    }

    @Override
    public boolean isPlayableInFull() {
        return (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _opponentId, _playerId))
                && _game.getGameState().getHand(_opponentId).size() >= _minChosen;
    }

    @Override
    public void playEffect() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _opponentId, _playerId)) {
            List<PhysicalCard> opponentHand = new LinkedList<>(_game.getGameState().getHand(_opponentId));
            _game.sendMessage(_source.getCardLink() + " revealed " + _opponentId + " cards in hand - " +
                    TextUtils.concatenateStrings(opponentHand.stream().map(PhysicalCard::getCardLink)));

            final ActionOrder actionOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_opponentId, false);
            // Skip hand owner (opponent)
            actionOrder.getNextPlayer();

            String nextPlayer;
            while ((nextPlayer = actionOrder.getNextPlayer()) != null) {
                if (nextPlayer.equals(_playerId)) {
                    Collection<PhysicalCard> selectable = Filters.filter(opponentHand, _game, _selectionFilter);

                    _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, _text, opponentHand, new LinkedList<>(selectable), Math.min(_minChosen, selectable.size()), Math.min(_maxChosen, selectable.size())) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                    cardsSelected(selectedCards);
                                }
                            });
                } else if (!nextPlayer.equals(_opponentId)) {
                    _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, "Hand of " + _opponentId, opponentHand, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }
            }
        }
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public EffectType getType() {
        return null;
    }

    protected abstract void cardsSelected(List<PhysicalCard> selectedCards);
}
