package com.gempukku.stccg.effects;

import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.EffectResult;

public class TriggeringResultEffect implements Effect {
    private final EffectType _effectType;
    private final EffectResult _effectResult;
    private final String _text;
    private final DefaultGame _game;

    public TriggeringResultEffect(DefaultGame game, EffectResult effectResult, String text) {
        this(game, null, effectResult, text);
    }

    public TriggeringResultEffect(DefaultGame game, EffectType effectType, EffectResult effectResult, String text) {
        _effectType = effectType;
        _effectResult = effectResult;
        _text = text;
        _game = game;
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
}
