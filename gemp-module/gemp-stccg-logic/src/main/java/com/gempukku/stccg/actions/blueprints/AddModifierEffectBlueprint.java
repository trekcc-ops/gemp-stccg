package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.modifiers.AddUntilModifierAction;
import com.gempukku.stccg.actions.targetresolver.TargetResolverBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

import java.util.ArrayList;
import java.util.List;

public class AddModifierEffectBlueprint implements SubActionBlueprint {

    private final TimeResolver.Time _until;
    private final ModifierBlueprint _modifierSource;
    private final TargetResolverBlueprint _cardTarget;

    public AddModifierEffectBlueprint(@JsonProperty(value = "modifier", required = true)
                                      ModifierBlueprint modifierBlueprint,
                                      @JsonProperty("until")
                                      JsonNode untilNode,
                                      @JsonProperty("target")
                                      TargetResolverBlueprint cardTarget
                                      ) throws InvalidCardDefinitionException {
        _until = TimeResolver.resolveTime(untilNode, "end(current)");
        _modifierSource = modifierBlueprint;
        _cardTarget = cardTarget;
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction, ActionContext context) {
        List<Action> result = new ArrayList<>();
        PhysicalCard performingCard = context.card();
        for (String playerName : cardGame.getAllPlayerIds()) {
            if (performingCard.isControlledBy(playerName)) {
                Action action = new AddUntilModifierAction(cardGame, playerName, _modifierSource, _until, context);
                result.add(action);
            }
        }
        return result;
    }

}