package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
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
        return "Seed " + _cardSeeded.getFullName();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        gameState.sendMessage(_cardSeeded.getOwnerName() + " seeded " + _cardSeeded.getCardLink());
        gameState.removeCardFromZone(_cardSeeded);
        gameState.getPlayer(_cardSeeded.getOwnerName()).addPlayedAffiliation(_cardSeeded.getCurrentAffiliation());
        gameState.seedFacilityAtLocation(_cardSeeded, _spacelineIndex);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(_playedFrom, _cardSeeded));

        return new FullEffectResult(true);
    }
}