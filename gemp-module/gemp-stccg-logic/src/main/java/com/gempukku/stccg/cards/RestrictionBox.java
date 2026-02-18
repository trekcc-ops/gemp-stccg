package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class RestrictionBox {

    private final ModifierBlueprint _modifierBlueprint;
    private final String _text;

    RestrictionBox(@JsonProperty("modifier") ModifierBlueprint modifierBlueprint,
                   @JsonProperty("text") String text) {
        _modifierBlueprint = modifierBlueprint;
        _text = text;
    }

    public Modifier getModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext context) {
        if (_modifierBlueprint != null) {
            return _modifierBlueprint.createModifier(cardGame, thisCard, context);
        } else {
            return null;
        }
    }

}