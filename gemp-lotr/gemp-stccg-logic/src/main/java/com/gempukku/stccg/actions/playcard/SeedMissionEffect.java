package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalMissionCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedMissionEffect extends SeedCardEffect {
    private final int _spacelineIndex;
    private final boolean _sharedMission;

    public SeedMissionEffect(String performingPlayerId, Zone playedFrom, PhysicalMissionCard cardPlayed,
                             int spacelineIndex, boolean sharedMission) {
        super(performingPlayerId, cardPlayed, Zone.SPACELINE);
        _spacelineIndex = spacelineIndex;
        _sharedMission = sharedMission;
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = _game.getGameState();

        _game.getGameState().sendMessage(_cardSeeded.getOwnerName() + " seeded " +
                _cardSeeded.getCardLink() +  " from " + _fromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardSeeded);
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        _game.getGameState().addToSpaceline((PhysicalMissionCard) _cardSeeded, _spacelineIndex, _sharedMission);
        _game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _fromZone, _cardSeeded));

        return new DefaultEffect.FullEffectResult(true);
    }
}