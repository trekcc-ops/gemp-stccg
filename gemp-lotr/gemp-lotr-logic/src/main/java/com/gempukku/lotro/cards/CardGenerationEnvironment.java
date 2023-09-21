package com.gempukku.lotro.cards;

import com.gempukku.lotro.effectappender.EffectAppenderFactory;
import com.gempukku.lotro.filters.FilterFactory;
import com.gempukku.lotro.modifiers.ModifierSourceFactory;
import com.gempukku.lotro.requirement.RequirementFactory;
import com.gempukku.lotro.requirement.trigger.TriggerCheckerFactory;

public interface CardGenerationEnvironment {
    EffectAppenderFactory getEffectAppenderFactory();

    FilterFactory getFilterFactory();

    RequirementFactory getRequirementFactory();

    TriggerCheckerFactory getTriggerCheckerFactory();

    ModifierSourceFactory getModifierSourceFactory();
}
