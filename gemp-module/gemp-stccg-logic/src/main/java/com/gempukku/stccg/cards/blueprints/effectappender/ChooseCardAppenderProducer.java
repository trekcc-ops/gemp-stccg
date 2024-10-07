package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Zone;

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
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "text", "player");

        final PlayerSource selectingPlayer = environment.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = environment.getTargetPlayerSource(effectObject);
        final String memorize = effectObject.get("memorize").textValue();
        final String filter = environment.getString(effectObject, "filter", "choose(any)");
        final FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");


        final String text = switch (effectType) {
            case CHOOSEACTIVECARDS -> environment.getString(effectObject, "text");
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK -> environment.getString(
                    effectObject, "text", "Choose cards from " + effectType.getZoneName());
        };

        if (text == null)
            throw new InvalidCardDefinitionException("You need to define text to show");

        return switch (effectType) {
            case CHOOSEACTIVECARDS ->
                    CardResolver.resolveCardsInPlay(filter, count, memorize, targetPlayer, text, cardFilter);
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK ->
                    CardResolver.resolveCardsInZone(filter, null, count, memorize, selectingPlayer,
                            targetPlayer, text, cardFilter, effectType.fromZone, false,
                            environment.getCardSourceFromZone(targetPlayer, effectType.fromZone, filter));
        };
    }
}