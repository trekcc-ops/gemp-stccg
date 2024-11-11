package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.*;


import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class DrawCardsEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;

    public DrawCardsEffect(DefaultGame game, Action action, String playerId, int count) {
        super(game);
        _action = action;
        _playerId = playerId;
        _count = count;
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
        SubAction subAction = new SubAction(_action, _game);
        final List<DrawOneCardEffect> drawEffects = new LinkedList<>();
        for (int i = 0; i < _count; i++) {
            final DrawOneCardEffect effect = new DrawOneCardEffect(_game, _playerId);
            subAction.appendEffect(effect);
            drawEffects.add(effect);
        }
        subAction.appendEffect(
                new UnrespondableEffect(_game) {
                    @Override
                    protected void doPlayEffect() {
                        int count = 0;
                        for (DrawOneCardEffect drawEffect : drawEffects) {
                            if (drawEffect.wasCarriedOut())
                                count++;
                        }
                        if (count > 0)
                            _game.sendMessage(
                                    _playerId + " draws " + count + " card" + ((count > 1) ? "s" : ""));
                    }
                }
        );
        processSubAction(_game, subAction);
    }
}