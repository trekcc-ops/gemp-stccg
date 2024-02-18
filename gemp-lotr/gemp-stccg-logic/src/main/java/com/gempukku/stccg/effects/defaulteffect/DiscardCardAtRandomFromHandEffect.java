package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DiscardCardFromHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DiscardCardAtRandomFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final boolean _forced;
    private final DefaultGame _game;

    public DiscardCardAtRandomFromHandEffect(ActionContext actionContext, String playerId, boolean forced) {
        _source = actionContext.getSource();
        _game = actionContext.getGame();
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
                && (!_forced || _game.getModifiersQuerying().canDiscardCardsFromHand(_game, _playerId, _source));
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            GameState gameState = _game.getGameState();
            List<? extends PhysicalCard> hand = gameState.getHand(_playerId);
            PhysicalCard randomCard = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
            gameState.sendMessage(_playerId + " randomly discards " + GameUtils.getCardLink(randomCard));
            gameState.removeCardsFromZone(_source.getOwnerName(), Collections.singleton(randomCard));
            gameState.addCardToZone(_game, randomCard, Zone.DISCARD);
            _game.getActionsEnvironment().emitEffectResult(new DiscardCardFromHandResult(_source, randomCard, _forced));
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
