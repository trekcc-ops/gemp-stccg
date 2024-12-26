package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
            protected List<Action> createActions(Action parentAction, ActionContext context) {
                DefaultGame cardGame = context.getGame();
                Action action = switch (effectType) {
                    case CHOOSEANUMBER -> new SelectNumberAction(context, choiceText, valueSource, memorize);
                    case CHOOSEOPPONENT -> {
                        List<String> playerIds = Arrays.asList(cardGame.getAllPlayerIds());
                        playerIds.remove(context.getPerformingPlayerId());
                        yield new SelectPlayerAction(context, memorize, playerIds);
                    }
                    case CHOOSEPLAYER ->
                            new SelectPlayerAction(context, memorize, Arrays.asList(cardGame.getAllPlayerIds()));
                    case CHOOSEPLAYEREXCEPT -> {
                        List<String> playerIds = Arrays.asList(cardGame.getAllPlayerIds());
                        playerIds.remove(excludePlayerSource.getPlayerId(context));
                        yield new SelectPlayerAction(context, memorize, playerIds);
                    }
                    case CHOOSETRIBBLEPOWER -> new SelectTribblePowerAction(context, memorize);
                };
                return List.of(action);
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