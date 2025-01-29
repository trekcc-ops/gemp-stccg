package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

public abstract class TriggerActionSource extends DefaultActionSource {

    public abstract TriggerTiming getTiming();
    public abstract RequiredType getRequiredType();

    protected TriggerActionSource(String text, int limitPerTurn, Phase phase) {
        super(text, limitPerTurn, phase);
    }

}