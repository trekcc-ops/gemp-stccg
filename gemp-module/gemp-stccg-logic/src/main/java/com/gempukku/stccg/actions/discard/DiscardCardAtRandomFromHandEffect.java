package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.discard.DiscardCardFromHandResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DiscardCardAtRandomFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final boolean _forced;

    public DiscardCardAtRandomFromHandEffect(ActionContext actionContext, String playerId, boolean forced) {
        super(actionContext, playerId);
        _source = actionContext.getSource();
        _playerId = playerId;
        _forced = forced;
    }

    @Override
    public String getText() {
        return "Discard card at random from hand";
    }

    @Override
    public boolean isPlayableInFull() {
        return !_game.getGameState().getHand(_playerId).isEmpty()
                && (!_forced || _game.getModifiersQuerying().canDiscardCardsFromHand(_playerId, _source));
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            List<? extends PhysicalCard> hand = gameState.getHand(_playerId);
            PhysicalCard randomCard = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
            gameState.sendMessage(_playerId + " randomly discards " + randomCard.getCardLink());
            gameState.removeCardsFromZone(_source.getOwnerName(), Collections.singleton(randomCard));
            gameState.addCardToZone(randomCard, Zone.DISCARD);
            _game.getActionsEnvironment().emitEffectResult(new DiscardCardFromHandResult(_source, randomCard, _forced));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
