package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.List;

public class ProxyCoreCard extends AbstractPhysicalCard {

    // Fictional card used to represent the destination when other card are played to a player's core

    public ProxyCoreCard(String playerName) {
        super(-1701, playerName, null);
        _zone = Zone.CORE;
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
    public List<Action> getEncounterActions(DefaultGame game, AttemptMissionAction attemptAction,
                                            AttemptingUnit attemptingUnit, MissionLocation missionLocation) {
        return new ArrayList<>();
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