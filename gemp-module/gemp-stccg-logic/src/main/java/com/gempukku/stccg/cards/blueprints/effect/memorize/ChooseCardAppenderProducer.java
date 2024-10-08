package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
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
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");
        switch(effectType) {
            case CHOOSEACTIVECARDS, CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK:
                environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "text", "player");
                break;
        }

        final PlayerSource selectingPlayer = environment.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = environment.getTargetPlayerSource(effectObject);
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

        return switch (effectType) {
            case CHOOSEACTIVECARDS ->
                    CardResolver.resolveCardsInPlay(BlueprintUtils.getString(effectObject, "filter", "choose(any)"),
                            ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment),
                            memorize, ActionContext::getPerformingPlayerId, text,
                            environment.getCardFilterableIfChooseOrAll(
                                    BlueprintUtils.getString(effectObject, "filter", "choose(any)")));
            case CHOOSECARDSFROMDISCARD, CHOOSECARDSFROMDRAWDECK ->
                    environment.buildTargetCardAppender(effectObject, selectingPlayer, targetPlayer, text,
                            effectType.fromZone, memorize, false);
        };
    }
}