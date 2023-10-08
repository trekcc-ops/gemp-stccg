package com.gempukku.stccg.cards;

import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.modifiers.ModifierSourceFactory;
import com.gempukku.stccg.requirement.RequirementFactory;
import com.gempukku.stccg.requirement.trigger.TriggerCheckerFactory;

public interface CardGenerationEnvironment {
    EffectAppenderFactory getEffectAppenderFactory();

    FilterFactory getFilterFactory();

    RequirementFactory getRequirementFactory();

    TriggerCheckerFactory getTriggerCheckerFactory();

    ModifierSourceFactory getModifierSourceFactory();
}
