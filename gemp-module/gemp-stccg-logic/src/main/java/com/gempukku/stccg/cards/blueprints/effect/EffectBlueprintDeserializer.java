package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Zone;

public class EffectBlueprintDeserializer {

    public static EffectBlueprint getEffectBlueprint(JsonNode effectObject) throws InvalidCardDefinitionException {
        if (!effectObject.has("type") || !effectObject.get("type").isTextual())
            throw new InvalidCardDefinitionException("Unable to find effect type");
        final String type = effectObject.get("type").textValue();
        return switch (type.toLowerCase()) {
            case "discard", "discardcardsfromdrawdeck", "discardfromhand", "download", "play", "playcardfromdiscard",
                    "putcardsfromdeckintohand", "putcardsfromdeckontopofdeck", "putcardsfromdeckonbottomofdeck",
                    "putcardsfromdiscardintohand", "putcardsfromdiscardonbottomofdeck",
                    "putcardsfromdiscardontopofdeck", "putcardsfromhandonbottomofdeck",
                    "putcardsfromhandonbottomofplaypile", "putcardsfromhandontopofdeck",
                    "putcardsfromplayonbottomofdeck", "removecardsindiscardfromgame", "removefromthegame",
                    "returntohand", "revealcards", "revealcardsfromhand", "shufflecardsfromdiscardintodrawdeck",
                    "shufflecardsfromhandintodrawdeck", "shufflecardsfromplayintodrawdeck" ->
                    CardResolverMultiEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "chooseanumber", "chooseopponent", "chooseplayer", "chooseplayerexcept", "chooseplayerwithcardsindeck",
                    "choosetribblepower" ->
                    ChooseEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "chooseactivecards", "choosecardsfromdiscard", "choosecardsfromdrawdeck" ->
                    ChooseCardEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "discardbottomcardsfromdeck", "discardcardatrandomfromhand", "discardtopcardfromplaypile",
                    "discardtopcardsfromdeck", "drawcards", "lookattopcardsofdrawdeck", "lookathand",
                    "lookatrandomcardsfromhand", "placeplayedcardbeneathdrawdeck",
                    "placetopcardofdrawdeckontopofplaypile", "reordertopcardsofdrawdeck",
                    "revealbottomcardsofdrawdeck", "revealhand" ->
                    CardEffectBlueprintProducer.createEffectBlueprint(effectObject);
            case "activatetribblepower" -> new ActivateTribblePowerEffectBlueprint(effectObject);
            case "addmodifier" -> new AddModifierEffectBlueprint(effectObject);
            case "costtoeffect" -> new EffectWithCostBlueprint(effectObject);
            case "shufflehandintodrawdeck" -> new ShuffleCardGroupIntoDrawDeckEffectBlueprint(effectObject, Zone.HAND);
            case "shuffleplaypileintodrawdeck" ->
                    new ShuffleCardGroupIntoDrawDeckEffectBlueprint(effectObject, Zone.PLAY_PILE);
            default ->
                    throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        };
    }

}