package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.actions.RequiredTriggerAction;
import com.gempukku.lotro.actions.ResolveSkirmishDamageAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.KillEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.results.NormalSkirmishResult;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.results.OverwhelmSkirmishResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolveSkirmishRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public ResolveSkirmishRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame lotroGame, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.SKIRMISH_FINISHED_NORMALLY) {
                            NormalSkirmishResult skirmishResult = (NormalSkirmishResult) effectResult;
                            ResolveSkirmishDamageAction action = new ResolveSkirmishDamageAction(skirmishResult);
                            return Collections.singletonList(action);
                        } else if (effectResult.getType() == EffectResult.Type.SKIRMISH_FINISHED_WITH_OVERWHELM) {
                            OverwhelmSkirmishResult skirmishResult = (OverwhelmSkirmishResult) effectResult;
                            Set<LotroPhysicalCard> losers = new HashSet<>(skirmishResult.getInSkirmishLosers());

                            RequiredTriggerAction action = new RequiredTriggerAction(null);
                            action.setText("Resolve skirmish overwhelm");
                            action.appendEffect(new KillEffect(losers, KillEffect.Cause.OVERWHELM));

                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                }
        );
    }
}
