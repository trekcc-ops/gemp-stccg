package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.effects.KillEffect;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.results.ZeroVitalityResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterDeathRule {
    private final Set<PhysicalCard> _charactersAlreadyOnWayToDeath = new HashSet<>();
    private final DefaultActionsEnvironment _actionsEnvironment;

    public CharacterDeathRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ZERO_VITALITY) {
                            ZeroVitalityResult zeroVitalityResult = (ZeroVitalityResult) effectResult;
                            final Set<PhysicalCard> characters = zeroVitalityResult.getCharacters();
                            RequiredTriggerAction action = new RequiredTriggerAction(null);
                            action.setText("Character death");
                            action.appendEffect(
                                    new KillEffect(characters, KillEffect.Cause.WOUNDS));
                            action.appendEffect(
                                    new UnrespondableEffect() {
                                        @Override
                                        protected void doPlayEffect(DefaultGame game) {
                                            _charactersAlreadyOnWayToDeath.removeAll(characters);
                                        }
                                    });

                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });
    }

}
