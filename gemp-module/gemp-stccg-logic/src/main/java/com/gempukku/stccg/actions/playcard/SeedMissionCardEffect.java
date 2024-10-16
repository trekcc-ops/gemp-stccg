package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedMissionCardEffect extends SeedCardEffect {
    private final int _spacelineIndex;
    private final boolean _sharedMission;

    public SeedMissionCardEffect(String performingPlayerId, MissionCard cardPlayed,
                                 int spacelineIndex, boolean sharedMission) {
        super(performingPlayerId, cardPlayed, Zone.SPACELINE);
        _spacelineIndex = spacelineIndex;
        _sharedMission = sharedMission;
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = getGame().getGameState();

        getGame().sendMessage(_cardSeeded.getOwnerName() + " seeded " +
                _cardSeeded.getCardLink() +  " from " + _fromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardSeeded);
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        try {
            getGame().getGameState().addToSpaceline((MissionCard) _cardSeeded, _spacelineIndex, _sharedMission);
            getGame().getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _fromZone, _cardSeeded));
            return new DefaultEffect.FullEffectResult(true);
        } catch(InvalidGameLogicException exp) {
            getGame().sendMessage(exp.getMessage());
            return new DefaultEffect.FullEffectResult(false);
        }
    }
}