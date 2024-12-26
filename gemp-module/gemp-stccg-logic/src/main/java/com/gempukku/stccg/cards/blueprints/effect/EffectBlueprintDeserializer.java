package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public class EffectBlueprintDeserializer {

    public static EffectBlueprint getEffectBlueprint(JsonNode effectObject) throws InvalidCardDefinitionException {
        if (!effectObject.has("type") || !effectObject.get("type").isTextual())
            throw new InvalidCardDefinitionException("Unable to find effect type");
        final String type = effectObject.get("type").textValue();
        return switch (type.toLowerCase()) {
            case "discard", "discardcardsfromdrawdeck", "discardfromhand", "download", "play", "playcardfromdiscard",
                    "putcardsfromdeckintohand",
                    "putcardsfromplayonbottomofdeck", "removecardsindiscardfromgame",
                    "shufflecardsfromdiscardintodrawdeck",
                    "shufflecardsfromhandintodrawdeck", "shufflecardsfromplayintodrawdeck" ->
                    CardResolverMultiEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "chooseanumber", "chooseopponent", "chooseplayer", "chooseplayerexcept", "chooseplayerwithcardsindeck",
                    "choosetribblepower" ->
                    ChooseEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "chooseactivecards", "choosecardsfromdiscard", "choosecardsfromdrawdeck" ->
                    ChooseCardEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "drawcards" ->
                    DrawActionBlueprintProducer.createEffectBlueprint(effectObject);
                            // TODO - Activate tribble power should come through as a rule, not a card def
            case "activatetribblepower" -> new ActivateTribblePowerEffectBlueprint(effectObject);
            case "addmodifier" -> new AddModifierEffectBlueprint(effectObject);
            case "costtoeffect" -> new EffectWithCostBlueprint(effectObject);
            default ->
                    throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        };
    }

    public static List<EffectBlueprint> getEffectBlueprints(JsonNode node) throws InvalidCardDefinitionException {
        List<EffectBlueprint> effectBlueprints = new LinkedList<>();
        List<JsonNode> effects = JsonUtils.toArray(node);
        for (JsonNode effect : effects)
                effectBlueprints.add(getEffectBlueprint(effect));
        return effectBlueprints;
    }

}