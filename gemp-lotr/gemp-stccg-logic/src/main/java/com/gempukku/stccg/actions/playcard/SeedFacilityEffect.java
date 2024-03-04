package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalFacilityCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedFacilityEffect extends SeedCardEffect {
    private final int _spacelineIndex;

    public SeedFacilityEffect(String performingPlayerId, Zone playedFrom, PhysicalFacilityCard cardSeeded, int spacelineIndex) {
        super(performingPlayerId, cardSeeded, Zone.AT_LOCATION);
        _spacelineIndex = spacelineIndex;
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        gameState.sendMessage(_cardSeeded.getOwnerName() + " seeded " + _cardSeeded.getCardLink());
        gameState.removeCardFromZone(_cardSeeded);
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        gameState.getPlayer(_cardSeeded.getOwnerName())
                .addPlayedAffiliation(((PhysicalFacilityCard) _cardSeeded).getCurrentAffiliation());
        gameState.seedFacilityAtLocation((PhysicalFacilityCard) _cardSeeded, _spacelineIndex);
        _game.getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _fromZone, _cardSeeded));

        return new DefaultEffect.FullEffectResult(true);
    }
}