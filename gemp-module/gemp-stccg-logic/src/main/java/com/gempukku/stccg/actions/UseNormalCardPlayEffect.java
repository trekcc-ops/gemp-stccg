package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

// TODO - Started to implement this class but not sure it's the way to go finally

public class UseNormalCardPlayEffect extends AbstractSubActionEffect {
    private final Player _player;
    private final ST1EGame _game;

    public UseNormalCardPlayEffect(ST1EGame game, Player player) {
        _player = player;
        _game = game;
    }

    @Override
    public String getText() { return "Use normal card play"; }

    @Override
    public EffectType getType() { return EffectType.USE_NORMAL_CARD_PLAY; }

    @Override
    public boolean isPlayableInFull() {
        return _game.getModifiersQuerying().getNormalCardPlaysAvailable(_player) >= 1;
    }
    @Override
    public void playEffect() {
        if (isPlayableInFull()) {
            _game.getModifiersEnvironment().useNormalCardPlay(_player);
            _game.sendMessage("Normal card play used");
        }
    }
}
