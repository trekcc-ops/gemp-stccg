package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class SeedMissionCardEffect extends SeedCardEffect {
    private final int _spacelineIndex;
    private final boolean _sharedMission;

    SeedMissionCardEffect(Player player, MissionCard cardPlayed,
                          int spacelineIndex, boolean sharedMission, Action causalAction) {
        super(player.getPlayerId(), cardPlayed, Zone.SPACELINE, causalAction);
        _spacelineIndex = spacelineIndex;
        _sharedMission = sharedMission;
    }


    @Override
    protected DefaultEffect.FullEffectResult playEffectReturningResult() {
        boolean result;
        ST1EGameState gameState = getGame().getGameState();

        getGame().sendMessage(_cardSeeded.getOwnerName() + " seeded " +
                _cardSeeded.getCardLink() +  " from " + _fromZone.getHumanReadable());

        gameState.removeCardFromZone(_cardSeeded);
        _game.getActionsEnvironment().addPerformedAction(_causalAction);

        try {
            if (_cardSeeded instanceof MissionCard missionCard) {
                if (_sharedMission)
                    gameState.addMissionCardToSharedMission(missionCard, _spacelineIndex);
                else
                    gameState.addMissionLocationToSpaceline(missionCard, _spacelineIndex);
                getGame().getActionsEnvironment().emitEffectResult(
                        new PlayCardResult(this, _fromZone, _cardSeeded));
                result = true;
            } else {
                getGame().sendMessage("Game error - called SeedMissionCardEffect with a non-mission card");
                result = false;
            }
        } catch(InvalidGameLogicException exp) {
            getGame().sendMessage(exp.getMessage());
            result = false;
        }
        return new DefaultEffect.FullEffectResult(result);
    }
}