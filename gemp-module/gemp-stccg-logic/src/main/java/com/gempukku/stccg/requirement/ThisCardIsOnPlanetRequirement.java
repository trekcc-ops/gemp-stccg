package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Objects;

public class ThisCardIsOnPlanetRequirement implements Requirement {

    @JsonProperty("planetName")
    String _planetName;

    @JsonCreator
    public ThisCardIsOnPlanetRequirement(@JsonProperty("planetName") String planetName) {
        _planetName = planetName;
    }
    @Override
    public boolean accepts(GameTextContext context, DefaultGame cardGame) {
        return cardGame instanceof ST1EGame stGame &&
                Objects.equals(context.card().getGameLocation(stGame).getLocationName(), _planetName);
    }

}