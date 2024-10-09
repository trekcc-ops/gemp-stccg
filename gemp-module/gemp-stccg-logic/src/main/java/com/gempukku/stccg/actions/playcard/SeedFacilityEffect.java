package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedFacilityEffect extends SeedCardEffect {
    private final int _spacelineIndex;

    public SeedFacilityEffect(String performingPlayerId, FacilityCard cardSeeded, int spacelineIndex) {
        super(performingPlayerId, cardSeeded, Zone.AT_LOCATION);
        _spacelineIndex = spacelineIndex;
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = getGame().getGameState();

        getGame().sendMessage(_cardSeeded.getOwnerName() + " seeded " + _cardSeeded.getCardLink());
        gameState.removeCardFromZone(_cardSeeded);
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        gameState.getPlayer(_cardSeeded.getOwnerName())
                .addPlayedAffiliation(((FacilityCard) _cardSeeded).getAffiliation());
        gameState.seedFacilityAtLocation((FacilityCard) _cardSeeded, _spacelineIndex);
        getGame().getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _fromZone, _cardSeeded));

        return new DefaultEffect.FullEffectResult(true);
    }
}