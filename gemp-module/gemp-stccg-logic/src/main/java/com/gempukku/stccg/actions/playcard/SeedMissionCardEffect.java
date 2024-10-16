package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1EMission;

import static com.gempukku.stccg.gamestate.ST1EMission.SharedStatus.NOT_SHARED;
import static com.gempukku.stccg.gamestate.ST1EMission.SharedStatus.SHARED;

public class SeedMissionCardEffect extends SeedCardEffect {
    private final int _spacelineIndex;
    private ST1EMission.SharedStatus _sharedStatus;

    public SeedMissionCardEffect(String performingPlayerId, MissionCard cardPlayed,
                                 int spacelineIndex, boolean sharedMission) {
        super(performingPlayerId, cardPlayed, Zone.SPACELINE);
        _spacelineIndex = spacelineIndex;
        _sharedStatus = (sharedMission) ? SHARED : NOT_SHARED;
    }

    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        ST1EGameState gameState = getGame().getGameState();

        getGame().sendMessage(_cardSeeded.getOwnerName() + " seeded " +
                _cardSeeded.getCardLink() +  " from " + _fromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardSeeded);
        _cardSeeded.getOwner().addCardSeeded(_cardSeeded);
        try {
            getGame().getGameState().addToSpaceline((MissionCard) _cardSeeded, _spacelineIndex, _sharedStatus);
            getGame().getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _fromZone, _cardSeeded));
            return new DefaultEffect.FullEffectResult(true);
        } catch(InvalidGameLogicException exp) {
            getGame().sendMessage(exp.getMessage());
            return new DefaultEffect.FullEffectResult(false);
        }
    }
}