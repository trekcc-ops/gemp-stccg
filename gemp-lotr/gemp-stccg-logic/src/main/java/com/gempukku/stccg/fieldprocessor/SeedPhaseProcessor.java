package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SeedPhaseProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        String phase = FieldUtils.getString(value, key);
        List<Phase> phasesList = new LinkedList<>();
        if (Objects.equals(phase, "any")) {
            phasesList.add(Phase.SEED_DOORWAY);
            phasesList.add(Phase.SEED_MISSION);
            phasesList.add(Phase.SEED_DILEMMA);
            phasesList.add(Phase.SEED_FACILITY);
        } else {
            phasesList.add(Enum.valueOf(Phase.class, ("SEED_" + phase).toUpperCase().replaceAll("[ '\\-]","_")));
        }
        blueprint.setSeedPhase(phasesList);
    }
}