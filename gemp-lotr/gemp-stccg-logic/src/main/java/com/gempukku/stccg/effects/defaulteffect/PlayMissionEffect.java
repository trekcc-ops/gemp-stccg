package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

public class PlayMissionEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private final int _spacelineIndex;
    private final boolean _sharedMission;
    private final ST1EGame _game;

    public PlayMissionEffect(ST1EGame game, Zone playedFrom, PhysicalCard cardPlayed,
                             int spacelineIndex, boolean sharedMission) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _spacelineIndex = spacelineIndex;
        _sharedMission = sharedMission;
        _game = game;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText() {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        final Zone playedFromZone = _cardPlayed.getZone();
        _game.getGameState().sendMessage(_cardPlayed.getOwner() + " played " +
                GameUtils.getCardLink(_cardPlayed) +  " from " + playedFromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardPlayed);
        _game.getGameState().addToSpaceline(_cardPlayed, _spacelineIndex, _sharedMission);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}