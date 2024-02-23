package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.actions.sources.TriggerActionSource;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.*;

public class BuiltCardBlueprint extends CardBlueprint {
    public BuiltCardBlueprint(String blueprintId) {
        super(blueprintId);
    }
}
