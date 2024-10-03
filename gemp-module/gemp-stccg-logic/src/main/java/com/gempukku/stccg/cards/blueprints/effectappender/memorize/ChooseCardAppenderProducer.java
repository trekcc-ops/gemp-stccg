package com.gempukku.stccg.cards.blueprints.effectappender.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Zone;

public class ChooseCardAppenderProducer implements EffectAppenderProducer {

    private enum EffectType {
        CHOOSEACTIVECARDS(null), CHOOSECARDSFROMDISCARD(Zone.DISCARD), CHOOSECARDSFROMDRAWDECK(Zone.DRAW_DECK);

        private final Zone fromZone;
        EffectType(Zone fromZone) { this.fromZone = fromZone; }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }

    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");
        switch(effectType) {
            case CHOOSEACTIVECARDS, CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK:
                environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "text");
                break;
        }

        final String memorize = effectObject.get("memorize").textValue();
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
                    CardResolver.resolveCardsInPlay(environment.getString(effectObject, "filter", "choose(any)"),
                            ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment),
                            memorize, ActionContext::getPerformingPlayerId, text,
                            environment.getCardFilterableIfChooseOrAll(
                                    environment.getString(effectObject, "filter", "choose(any)")));
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK ->
                    environment.buildTargetCardAppender(effectObject, text, effectType.fromZone, memorize);
        };
    }
}
