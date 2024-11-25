package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class ChooseEffectBlueprintProducer {

    private enum EffectType {
        CHOOSEANUMBER, CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT, CHOOSETRIBBLEPOWER
    }
    public static EffectBlueprint createEffectBlueprint(JsonNode effectObject)
            throws InvalidCardDefinitionException {
        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");

        switch(effectType) {
            case CHOOSEANUMBER:
                BlueprintUtils.validateAllowedFields(effectObject, "amount", "memorize", "text");
                break;
            case CHOOSEPLAYEREXCEPT:
                BlueprintUtils.validateAllowedFields(effectObject, "memorize", "text", "exclude");
                break;
            case CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSETRIBBLEPOWER:
                BlueprintUtils.validateAllowedFields(effectObject, "memorize", "text");
                break;
        }
        BlueprintUtils.validateRequiredFields(effectObject, "memorize");



        final String memorize = effectObject.get("memorize").textValue();

        final String choiceText = BlueprintUtils.getString(effectObject, "text", getDefaultText(effectType));
        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("amount"), 0);
        final PlayerSource excludePlayerSource =
                BlueprintUtils.getPlayerSource(effectObject, "exclude", true);


        return new DelayedEffectBlueprint() {
            @Override
            protected Effect createEffect(Action action, ActionContext context) {
                return switch (effectType) {
                    case CHOOSEANUMBER -> new ChooseNumberEffect(context, choiceText, valueSource, memorize);
                    case CHOOSEOPPONENT -> new ChooseOpponentEffect(context, memorize);
                    case CHOOSEPLAYER -> new ChoosePlayerEffect(context, memorize);
                    case CHOOSEPLAYEREXCEPT ->
                            new ChoosePlayerExceptEffect(context, excludePlayerSource.getPlayerId(context), memorize);
                    case CHOOSETRIBBLEPOWER -> new ChooseTribblePowerEffect(context, memorize);
                };
            }
        };
    }

    private static String getDefaultText(EffectType effectType) {
        return switch (effectType) {
            case CHOOSEANUMBER -> "Choose a number";
            case CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT -> "Choose a player";
            case CHOOSETRIBBLEPOWER -> "Choose a tribble power";
        };

    }

}