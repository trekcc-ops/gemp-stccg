package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.actions.sources.SeedCardActionSource;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectprocessor.*;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SeedFieldProcessor implements FieldProcessor {
    private final Map<String, EffectProcessor> _seedProcessors = new HashMap<>();
    public SeedFieldProcessor() { }
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final JSONObject[] seedArray = environment.getObjectArray(value, key);
        if (seedArray.length > 1)
            throw new InvalidCardDefinitionException("Currently code is not designed to take more than one seed item");
        else {
            for (JSONObject seedItem : seedArray) {
                SeedCardActionSource actionSource = new SeedCardActionSource();
                if (seedItem.get("limit") != null) {
                    final Integer limit = environment.getInteger(seedItem.get("limit"), "limit");
                    actionSource.addRequirement(
                            (actionContext) -> actionContext.getSource()
                                    .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) < limit);
                }
                if (seedItem.get("where") != null) {
                    final String where = environment.getString(seedItem.get("where"), "where");
                    if (Objects.equals(where, "table"))
                        actionSource.setSeedZone(Zone.TABLE);
                    else throw new InvalidCardDefinitionException("Unknown parameter in seed:where field");
                }
                blueprint.setSeedCardActionSource(actionSource);
            }
        }
    }
}