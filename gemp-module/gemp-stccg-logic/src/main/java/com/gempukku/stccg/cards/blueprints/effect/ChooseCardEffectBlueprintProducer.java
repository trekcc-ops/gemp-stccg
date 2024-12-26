package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.*;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Zone;

public class ChooseCardEffectBlueprintProducer {

    private enum EffectType {
        CHOOSEACTIVECARDS(null),
        CHOOSECARDSFROMDISCARD(Zone.DISCARD),
        CHOOSECARDSFROMDRAWDECK(Zone.DRAW_DECK);

        private final Zone fromZone;
        EffectType(Zone fromZone) { this.fromZone = fromZone; }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }

    public static EffectBlueprint createEffectBlueprint(JsonNode effectObject)
            throws InvalidCardDefinitionException {

        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");
        BlueprintUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "text", "player");
        BlueprintUtils.validateRequiredFields(effectObject, "memorize");

        if (effectType == EffectType.CHOOSEACTIVECARDS)
            BlueprintUtils.validateRequiredFields(effectObject, "text");

        final PlayerSource selectingPlayer = BlueprintUtils.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = BlueprintUtils.getTargetPlayerSource(effectObject);
        final String memorize = effectObject.get("memorize").textValue();
        final String filter = BlueprintUtils.getString(effectObject, "filter", "choose(any)");
        final FilterableSource cardFilter = (filter.startsWith("all(") || filter.startsWith("choose(")) ?
                new FilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")"))) :
                null;
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1);

        final String text = switch (effectType) {
            case CHOOSEACTIVECARDS -> BlueprintUtils.getString(effectObject, "text");
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK -> BlueprintUtils.getString(
                    effectObject, "text", "Choose cards from " + effectType.getZoneName());
        };

        return switch (effectType) {
            case CHOOSEACTIVECARDS ->
                    CardResolver.resolveCardsInPlay(filter, count, memorize, targetPlayer, text, cardFilter);
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK ->
                    CardResolver.resolveCardsInZone(filter, null, count, memorize, selectingPlayer,
                            targetPlayer, text, cardFilter, effectType.fromZone,
                            BlueprintUtils.getCardSourceFromZone(targetPlayer, effectType.fromZone, filter));
        };
    }
}