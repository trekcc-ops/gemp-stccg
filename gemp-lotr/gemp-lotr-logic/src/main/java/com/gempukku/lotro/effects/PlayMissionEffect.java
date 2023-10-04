package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Quadrant;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.ST1EGame;
import com.gempukku.lotro.results.PlayCardResult;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PlayMissionEffect extends AbstractEffect<ST1EGame> {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private final int _spacelineIndex;
    private final Quadrant _quadrant;

    public PlayMissionEffect(Zone playedFrom, PhysicalCard cardPlayed, Quadrant quadrant, int spacelineIndex) {
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _spacelineIndex = spacelineIndex;
        _quadrant = quadrant;
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
        game.getGameState().addToSpaceline(_cardPlayed, _quadrant, _spacelineIndex);
        game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        game.getGameState().addCardToZone(game, _cardPlayed, Zone.VOID);

        // Defined by the game. For Tribbles, this is where the chain is advanced.
        game.getGameState().playEffectReturningResult(_cardPlayed);

        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardPlayed));

        return new FullEffectResult(true);
    }
}