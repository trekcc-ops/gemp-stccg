package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Preventable;
import com.gempukku.stccg.results.DrawCardOrPutIntoHandResult;

public class DrawOneCardEffect extends DefaultEffect implements Preventable {
    private final String _playerId;
    private boolean _prevented;
    private final DefaultGame _game;

    public DrawOneCardEffect(DefaultGame game, String playerId) {
        _playerId = playerId;
        _game = game;
    }

    @Override
    public String getText() {
        return "Draw a card";
    }

    @Override
    public EffectType getType() {
        return EffectType.BEFORE_DRAW_CARD;
    }

    @Override
    public boolean isPlayableInFull() {
        return !_game.getGameState().getDrawDeck(_playerId).isEmpty();
    }

    public boolean canDrawCard() {
        return (!_prevented && !_game.getGameState().getDrawDeck(_playerId).isEmpty()) &&
                _game.getModifiersQuerying().canDrawCardNoIncrement(_game, _playerId);
    }

    @Override
    public String getPerformingPlayer() {
        return _playerId;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        int drawn = 0;
        if (!_prevented && !_game.getGameState().getDrawDeck(_playerId).isEmpty() &&
                (_game.getFormat().doesNotHaveRuleOfFour() ||
                        _game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(_game, _playerId))) {
            _game.getGameState().playerDrawsCard(_playerId);
            drawn++;
        }

        if (drawn == 1) {
            _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_playerId, true));
            return new FullEffectResult(true);
        } else
            return new FullEffectResult(false);
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPrevented() {
        return _prevented;
    }
}
