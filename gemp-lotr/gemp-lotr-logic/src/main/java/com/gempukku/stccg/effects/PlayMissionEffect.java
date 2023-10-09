package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

public class PlayMissionEffect extends AbstractEffect<ST1EGame> {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private final int _spacelineIndex;
    private final boolean _sharedMission;

    public PlayMissionEffect(Zone playedFrom, PhysicalCard cardPlayed,
                             int spacelineIndex, boolean sharedMission) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _spacelineIndex = spacelineIndex;
        _sharedMission = sharedMission;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull(ST1EGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(ST1EGame game) {
        ST1EGameState gameState = game.getGameState();

        final Zone playedFromZone = _cardPlayed.getZone();
        game.getGameState().sendMessage(_cardPlayed.getOwner() + " played " +
                GameUtils.getCardLink(_cardPlayed) +  " from " + playedFromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardPlayed);
        game.getGameState().addToSpaceline(_cardPlayed, _spacelineIndex, _sharedMission);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}