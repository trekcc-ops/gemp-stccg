package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class ProxyAnonymousCard extends AbstractPhysicalCard {

        /* Used to send cards to the client without showing identifying information.
        For example, this might be used to communicate to Player 1 that Player 2 has a hidden agenda card in play
        without telling the player anything about that card.
     */

    public ProxyAnonymousCard(String ownerName) {
        super(-999, ownerName, null);
    }

    @Override
    public TopLevelSelectableAction getPlayCardAction(DefaultGame cardGame, boolean forFree) {
        return null;
    }

    @Override
    public boolean isMisSeed(DefaultGame cardGame, MissionLocation mission) {
        return false;
    }

    @Override
    public List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction, AttemptingUnit attemptingUnit, MissionLocation missionLocation) throws InvalidGameLogicException, PlayerNotFoundException {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isOnPlanet(DefaultGame cardGame) {
        return false;
    }


}