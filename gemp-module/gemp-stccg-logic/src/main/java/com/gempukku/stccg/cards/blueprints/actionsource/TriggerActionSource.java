package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

public abstract class TriggerActionSource extends DefaultActionSource {

    public abstract TriggerTiming getTiming();
    public abstract RequiredType getRequiredType();

}
