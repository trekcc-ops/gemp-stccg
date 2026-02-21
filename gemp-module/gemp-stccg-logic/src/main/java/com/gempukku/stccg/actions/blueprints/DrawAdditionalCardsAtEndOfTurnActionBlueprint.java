package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.evaluator.SingleValueSource;
import com.gempukku.stccg.requirement.PhaseRequirement;

import java.util.ArrayList;
import java.util.List;

public class DrawAdditionalCardsAtEndOfTurnActionBlueprint extends ActivateCardActionBlueprint {

    @JsonCreator
    private DrawAdditionalCardsAtEndOfTurnActionBlueprint(@JsonProperty("count") SingleValueSource count)
            throws InvalidCardDefinitionException {
        super(
                new UsageLimitBlueprint("eachOfYourTurns", 1),
                List.of(new PhaseRequirement(Phase.END_OF_TURN)), new ArrayList<>(),
                List.of(new DrawCardsActionBlueprint(count, "you"))
        );
    }

}