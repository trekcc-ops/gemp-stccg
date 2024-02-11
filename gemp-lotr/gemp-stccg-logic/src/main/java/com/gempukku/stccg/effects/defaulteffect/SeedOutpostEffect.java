package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.cards.PhysicalNounCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.results.PlayCardResult;
import com.gempukku.stccg.rules.GameUtils;

public class SeedOutpostEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalFacilityCard _cardSeeded;
    private final int _spacelineIndex;
    private final ST1EGame _game;

    public SeedOutpostEffect(ST1EGame game, Zone playedFrom, PhysicalFacilityCard cardSeeded, int spacelineIndex) {
        _playedFrom = playedFrom;
        _cardSeeded = cardSeeded;
        _spacelineIndex = spacelineIndex;
        _game = game;
    }

    public PhysicalCard getPlayedCard() {
        return _cardSeeded;
    }

    @Override
    public String getText() {
        return "Seed " + GameUtils.getFullName(_cardSeeded);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        _game.getGameState().sendMessage(_cardSeeded.getOwner() + " seeded " +
                GameUtils.getCardLink(_cardSeeded));

        gameState.removeCardFromZone(_cardSeeded);
        _game.getGameState().getPlayer(_cardSeeded.getOwner()).addPlayedAffiliation(_cardSeeded.getCurrentAffiliation());
        _game.getGameState().seedFacilityAtLocation(_cardSeeded, _spacelineIndex);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardSeeded));

        return new FullEffectResult(true);
    }
}