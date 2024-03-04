package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class DilemmaEffectFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {

/*        final JSONObject[] effectArray = environment.getObjectArray(value, key);
        if (effectArray.length > 1)
            throw new InvalidCardDefinitionException("Currently code is not designed to take more than one dilemma effect item");
        else {
            for (JSONObject effectItem : effectArray) {
                ActionSource actionSource = new RequiredTriggerActionSource(TriggerTiming.AFTER);
                if (effectItem.get("type") != null) {
                    final String effectType = environment.getString(effectItem.get("type"), "type");
                    actionSource.addEffect();
                    actionSource.addPlayRequirement(
                            (actionContext) -> actionContext.getSource()
                                    .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) < limit);
                }
                if (effectItem.get("where") != null) {
                    final String where = environment.getString(effectItem.get("where"), "where");
                    if (Objects.equals(where, "table"))
                        actionSource.setSeedZone(Zone.TABLE);
                    else throw new InvalidCardDefinitionException("Unknown parameter in seed:where field");
                }
                blueprint.setSeedCardActionSource(actionSource);
            }
        } */
    }
}

/*dilemma-effect: {
type: kill
filter: random(any)
discard-dilemma: true
        }

*/