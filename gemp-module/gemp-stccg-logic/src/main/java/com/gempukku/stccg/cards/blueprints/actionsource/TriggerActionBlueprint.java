package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

public abstract class TriggerActionBlueprint extends DefaultActionBlueprint {

    public abstract TriggerTiming getTiming();
    public abstract RequiredType getRequiredType();

    protected TriggerActionBlueprint(String text, int limitPerTurn, Phase phase) {
        super(text, limitPerTurn, phase);
    }

}