package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Preventable;

public class DrawOneCardEffect extends DefaultEffect implements Preventable {
    private final String _playerId;
    private boolean _prevented;

    public DrawOneCardEffect(DefaultGame game, String playerId) {
        super(game, playerId);
        _playerId = playerId;
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
                _game.getModifiersQuerying().canDrawCardNoIncrement(_playerId);
    }

    @Override
    public String getPerformingPlayerId() {
        return _playerId;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        int drawn = 0;
        if (!_prevented && !_game.getGameState().getDrawDeck(_playerId).isEmpty()) {
            _game.getGameState().playerDrawsCard(_playerId);
            drawn++;
        }

        if (drawn == 1) {
            _game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(this, true));
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
