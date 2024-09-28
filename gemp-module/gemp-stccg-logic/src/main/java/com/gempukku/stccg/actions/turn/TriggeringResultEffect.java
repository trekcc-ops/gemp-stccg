package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;

public class TriggeringResultEffect implements Effect {
    private final EffectType _effectType;
    private final EffectResult _effectResult;
    private final String _text;
    private final DefaultGame _game;

    public TriggeringResultEffect(EffectResult effectResult, String text) {
        _effectType = null;
        _effectResult = effectResult;
        _text = text;
        _game = effectResult.getGame();
    }

    @Override
    public EffectType getType() {
        return _effectType;
    }

    @Override
    public String getText() {
        return _text;
    }
    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public boolean wasCarriedOut() {
        return true;
    }

    @Override
    public void playEffect() {
        _game.getActionsEnvironment().emitEffectResult(_effectResult);
    }

    public String getPerformingPlayerId() { return null; }
    public DefaultGame getGame() { return _game; }
}
