package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AtLocationDestinationBlueprint implements DestinationBlueprint {

    private final Affiliation _affiliation;

    public AtLocationDestinationBlueprint(
            @JsonProperty(value = "affiliation", required = true) Affiliation affiliation
    ) {
        _affiliation = affiliation;
    }

    public Collection<PhysicalCard> getDestinationOptions(ST1EGame stGame, String performingPlayerName,
                                                          PhysicalCard cardArriving, GameTextContext context) {
        Collection<PhysicalCard> result = new ArrayList<>();
        for (MissionLocation location : stGame.getGameState().getUnorderedMissionLocations()) {
                if (location.hasMatchingAffiliationIcon(stGame, performingPlayerName, List.of(_affiliation))) {
                    try {
                        result.add(location.getMissionForPlayer(performingPlayerName));
                    } catch(InvalidGameLogicException exp) {
                        stGame.sendErrorMessage(exp);
                    }
                }
        }
        return result;
    }

    public Collection<MissionLocation> getDestinationOptions(ST1EGame stGame, String performingPlayerName) {
        Collection<MissionLocation> result = new ArrayList<>();
        for (MissionLocation location : stGame.getGameState().getUnorderedMissionLocations()) {
            if (location.hasMatchingAffiliationIcon(stGame, performingPlayerName, List.of(_affiliation))) {
                result.add(location);
            }
        }
        return result;
    }

}