package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LookAtOpponentsHandEffect extends DefaultEffect {
    private final String _playerId;
    private final String _opponentId;

    public LookAtOpponentsHandEffect(ActionContext actionContext, String opponentId) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
        _opponentId = opponentId;
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _opponentId, _playerId);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _opponentId, _playerId)) {
            List<PhysicalCard> opponentHand = new LinkedList<>(_game.getGameState().getHand(_opponentId));

            _game.sendMessage(_playerId + " looked at " + _opponentId + "'s entire hand");

            if (!opponentHand.isEmpty()) {
                _game.sendMessage(_playerId + " looked at " + _opponentId + "'s entire hand");

                _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Opponent's hand", opponentHand,
                            Collections.emptyList(), 0, 0) {
                        @Override
                        public void decisionMade(String result) {
                        }
                    });
            }
            else {
                _game.sendMessage("No cards in " + _opponentId + " hand to look at");
            }

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
