package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.modifiers.AddUntilModifierAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class AddModifierEffectBlueprint implements SubActionBlueprint {

    private final TimeResolver.Time _until;
    private final ModifierBlueprint _modifierSource;

    public AddModifierEffectBlueprint(@JsonProperty(value = "modifier", required = true)
                                      ModifierBlueprint modifierBlueprint,
                                      @JsonProperty("until")
                                      JsonNode untilNode
                                      ) throws InvalidCardDefinitionException {
        _until = TimeResolver.resolveTime(untilNode, "end(current)");
        _modifierSource = modifierBlueprint;
    }

    @Override
    public Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context) {
        PhysicalCard performingCard = context.card();
        if (performingCard.isControlledBy(context.yourName())) {
            return new AddUntilModifierAction(cardGame, context.yourName(), _modifierSource, _until, context);
        }
        else {
            return null;
        }
    }

}