package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

public class ScorePointsEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final String _scoringPlayer;
    private final int _points;
    private final DefaultGame _game;
    public ScorePointsEffect(DefaultGame game, PhysicalCard source, String scoringPlayer, int points) {
        _source = source;
        _scoringPlayer = scoringPlayer;
        _points = points;
        _game = game;
    }

    @Override
    public String getText() {
        return _scoringPlayer + " scored " + _points + " from " + GameUtils.getCardLink(_source);
    }
    @Override
    public boolean isPlayableInFull() { return true; }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().addToPlayerScore(_scoringPlayer, _points);
        return new FullEffectResult(true);
    }

}