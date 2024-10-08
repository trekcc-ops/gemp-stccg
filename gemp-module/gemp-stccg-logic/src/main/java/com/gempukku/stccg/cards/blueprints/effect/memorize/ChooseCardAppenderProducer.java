package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.List;
import java.util.function.Function;

public class ChooseCardAppenderProducer implements EffectAppenderProducer {

    private enum EffectType {
        CHOOSEACTIVECARDS(null),
        CHOOSECARDSFROMDISCARD(Zone.DISCARD),
        CHOOSECARDSFROMDRAWDECK(Zone.DRAW_DECK);

        private final Zone fromZone;
        EffectType(Zone fromZone) { this.fromZone = fromZone; }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }

    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");
        switch(effectType) {
            case CHOOSEACTIVECARDS, CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK:
                BlueprintUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "text", "player");
                break;
        }

        final PlayerSource selectingPlayer = BlueprintUtils.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = BlueprintUtils.getTargetPlayerSource(effectObject);
        final String memorize = effectObject.get("memorize").textValue();
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");


        final String text = switch (effectType) {
            case CHOOSEACTIVECARDS -> BlueprintUtils.getString(effectObject, "text");
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK -> BlueprintUtils.getString(
                    effectObject, "text", "Choose cards from " + effectType.getZoneName());
        };

        if (text == null)
            throw new InvalidCardDefinitionException("You need to define text to show");

        String filter = BlueprintUtils.getString(effectObject, "filter", "choose(any)");
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);
        boolean optional = BlueprintUtils.getBoolean(effectObject, "optional", false);

        Function<ActionContext, List<PhysicalCard>> cardSource =
                BlueprintUtils.getCardSourceFromZone(targetPlayer, effectType.fromZone, filter);

        ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1);
        if (optional) count = ValueResolver.resolveEvaluator("0-" + count);

        return switch (effectType) {
            case CHOOSEACTIVECARDS ->
                    CardResolver.resolveCardsInPlay(filter, count,
                            memorize, ActionContext::getPerformingPlayerId, text,
                            environment.getCardFilterableIfChooseOrAll(filter));
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK ->
                    CardResolver.resolveCardsInZone(filter, null, count, memorize, selectingPlayer,
                            targetPlayer, text, cardFilter, effectType.fromZone, false, cardSource);
        };
    }
}