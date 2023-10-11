package com.gempukku.stccg.effects.abstractsubaction;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.effects.defaulteffect.DrawOneCardEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class DrawCardsEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;
    private final DefaultGame _game;

    public DrawCardsEffect(DefaultGame game, Action action, String playerId, int count) {
        _action = action;
        _playerId = playerId;
        _count = count;
        _game = game;
    }

    @Override
    public String getText() {
        return "Draw " + _count + " card" + ((_count > 1) ? "s" : "");
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        return _game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    public void playEffect() {
        SubAction subAction = new SubAction(_action);
        final List<DrawOneCardEffect> drawEffects = new LinkedList<>();
        for (int i = 0; i < _count; i++) {
            final DrawOneCardEffect effect = new DrawOneCardEffect(_game, _playerId);
            subAction.appendEffect(effect);
            drawEffects.add(effect);
        }
        subAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        int count = 0;
                        for (DrawOneCardEffect drawEffect : drawEffects) {
                            if (drawEffect.wasCarriedOut())
                                count++;
                        }
                        if (count > 0)
                            _game.getGameState().sendMessage(
                                    _playerId + " draws " + count + " card" + ((count > 1) ? "s" : ""));
                    }
                }
        );
        processSubAction(_game, subAction);
    }
}
