package com.gempukku.stccg.effects.abstractsubaction;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.rules.GameUtils;

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
    private final ActionContext _actionContext;
    private final DefaultGame _game;

    protected RevealAndChooseCardsFromOpponentHandEffect(ActionContext actionContext, String playerId, String opponentId, PhysicalCard source, String text, Filterable selectionFilter, int minChosen, int maxChosen) {
        _playerId = playerId;
        _opponentId = opponentId;
        _source = source;
        _text = text;
        _selectionFilter = selectionFilter;
        _minChosen = minChosen;
        _maxChosen = maxChosen;
        _game = actionContext.getGame();
        _actionContext = actionContext;
    }

    @Override
    public boolean isPlayableInFull() {
        return (_actionContext.getGame().getModifiersQuerying().canLookOrRevealCardsInHand(_actionContext.getGame(), _opponentId, _playerId))
                && _actionContext.getGameState().getHand(_opponentId).size() >= _minChosen;
    }

    @Override
    public void playEffect() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _opponentId, _playerId)) {
            List<PhysicalCard> opponentHand = new LinkedList<>(_game.getGameState().getHand(_opponentId));
            _game.getGameState().sendMessage(_source.getCardLink() + " revealed " + _opponentId + " cards in hand - " + getAppendedNames(opponentHand));

            final PlayOrder playOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_opponentId, false);
            // Skip hand owner (opponent)
            playOrder.getNextPlayer();

            String nextPlayer;
            while ((nextPlayer = playOrder.getNextPlayer()) != null) {
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
