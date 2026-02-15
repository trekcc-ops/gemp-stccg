package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class OrRequirement implements Requirement {

    @JsonProperty("conditions")
    private final List<Requirement> _requirements;

    private OrRequirement(@JsonProperty("conditions")List<Requirement> requirements) {
        _requirements = requirements;
    }
    @Override
    public boolean accepts(ActionContext context, DefaultGame cardGame) {
        for (Requirement requirement : _requirements) {
            if (requirement.accepts(context, cardGame)) {
                return true;
            }
        }
        return false;
    }
}