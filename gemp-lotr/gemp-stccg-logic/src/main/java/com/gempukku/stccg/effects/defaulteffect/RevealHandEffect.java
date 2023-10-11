package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.results.RevealCardFromHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RevealHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _actingPlayer;
    private final String _handPlayerId;
    private final DefaultGame _game;

    public RevealHandEffect(ActionContext actionContext, String handPlayerId) {
        _source = actionContext.getSource();
        _actingPlayer = actionContext.getPerformingPlayer();
        _handPlayerId = handPlayerId;
        _game = actionContext.getGame();
    }

    @Override
    public String getText() {
        return "Reveal cards from hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _handPlayerId, _actingPlayer);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _handPlayerId, _actingPlayer)) {
            final List<? extends PhysicalCard> hand = _game.getGameState().getHand(_handPlayerId);
            _game.getGameState().sendMessage(
                    GameUtils.getCardLink(_source) + " revealed " + _handPlayerId + " cards in hand - " +
                            GameUtils.getAppendedNames(hand)
            );

            final PlayOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
            // Skip hand owner
            playerOrder.getNextPlayer();

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, "Hand of " + _handPlayerId, hand, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            cardsRevealed(hand);

            for (PhysicalCard card : hand) {
                _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _handPlayerId, card));
            }

            return new FullEffectResult(true);
        } else {
            return new FullEffectResult(false);
        }
    }

    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {

    }
}
