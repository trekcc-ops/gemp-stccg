package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.rules.TextUtils;

import java.util.Collections;
import java.util.List;

public abstract class RevealRandomCardsFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;
    private final DefaultGame _game;

    protected RevealRandomCardsFromHandEffect(ActionContext actionContext, String handOfPlayer, int count) {
        super(actionContext);
        _actingPlayer = actionContext.getPerformingPlayerId();
        _playerHand = handOfPlayer;
        _source = actionContext.getSource();
        _count = count;
        _game = actionContext.getGame();
    }

    @Override
    public String getText() {
        return "Reveal cards from hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_actingPlayer.equals(_playerHand) || _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer)) {
            List<PhysicalCard> randomCards = TextUtils.getRandomFromList(_game.getGameState().getHand(_playerHand), _count);

            if (!randomCards.isEmpty()) {
                final ActionOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwnerName(), false);

                String nextPlayer;
                while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                    _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, _playerHand+" revealed card(s) from hand at random", randomCards, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }

                _game.sendMessage(_source.getCardLink() + " revealed cards from " + _playerHand + " hand at random - " + TextUtils.getConcatenatedCardLinks(randomCards));
            }
            else {
                _game.sendMessage("No cards in " + _playerHand + " hand to reveal");
            }
            cardsRevealed(randomCards);
            for (PhysicalCard randomCard : randomCards)
                _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _playerHand, randomCard));

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    @Override
    public boolean isPlayableInFull() {
        if (_game.getGameState().getHand(_playerHand).size() < _count)
            return false;
        return _actingPlayer.equals(_playerHand) || _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}
