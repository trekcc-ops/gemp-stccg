package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.KillEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.results.ZeroVitalityResult;
import com.gempukku.stccg.effects.EffectResult;
import com.gempukku.stccg.effects.UnrespondableEffect;

import java.util.*;

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

    public void checkCharactersZeroVitality(DefaultGame game) {
        if (game.getGameState().getCurrentPhase() != Phase.PUT_RING_BEARER && game.getGameState().getCurrentPhase() != Phase.BETWEEN_TURNS) {
            GameState gameState = game.getGameState();

            Collection<PhysicalCard> characters = Filters.filterActive(game,
                    Filters.or(CardType.ALLY, CardType.COMPANION, CardType.MINION));

            Set<PhysicalCard> deadChars = new HashSet<>();
            for (PhysicalCard character : characters)
                if (!_charactersAlreadyOnWayToDeath.contains(character) && game.getModifiersQuerying().getVitality(game, character) <= 0)
                    deadChars.add(character);

            if (deadChars.size() > 0) {
                _charactersAlreadyOnWayToDeath.addAll(deadChars);
                game.getActionsEnvironment().emitEffectResult(new ZeroVitalityResult(deadChars));
            }
        }
    }
}
