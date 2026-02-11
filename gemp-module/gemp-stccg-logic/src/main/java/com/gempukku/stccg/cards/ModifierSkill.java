package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class ModifierSkill extends Skill {

    private final String _text;
    private final ModifierBlueprint _modifier;

    public ModifierSkill(String text) {
        _text = text;
        _modifier = null;
    }

    @JsonCreator
    public ModifierSkill(
            @JsonProperty("text")
            String text,
            @JsonProperty("modifier")
            ModifierBlueprint modifier
            ) {
        _text = text;
        _modifier = modifier;
    }

    public Modifier createModifierNew(DefaultGame cardGame, ActionContext context) {
        return _modifier.createModifier(cardGame, context.card(), context);
    }

    public Modifier getModifierOld(PhysicalCard card) throws InvalidGameLogicException {
        return null;
    }

}