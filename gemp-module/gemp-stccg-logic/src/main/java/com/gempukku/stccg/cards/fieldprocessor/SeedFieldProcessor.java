package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.sources.SeedCardActionSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.Objects;

public class SeedFieldProcessor implements FieldProcessor {
    public SeedFieldProcessor() { }
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        if (value.isArray())
            throw new InvalidCardDefinitionException("Currently code is not designed to take more than one seed item");
        else {
            SeedCardActionSource actionSource = new SeedCardActionSource();
            if (value.has("limit")) {
                actionSource.addRequirement(
                        (actionContext) -> actionContext.getSource()
                                .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) <
                                value.get("limit").asInt()
                );
            }
            if (value.has("where")) {
                if (Objects.equals(value.get("where").textValue(), "table"))
                    actionSource.setSeedZone(Zone.TABLE);
                else throw new InvalidCardDefinitionException("Unknown parameter in seed:where field");
            }
            blueprint.setSeedCardActionSource(actionSource);
        }
    }
}