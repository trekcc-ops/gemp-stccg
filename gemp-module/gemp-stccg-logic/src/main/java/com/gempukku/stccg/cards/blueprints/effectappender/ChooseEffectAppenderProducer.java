package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;

public class ChooseEffectAppenderProducer implements EffectAppenderProducer {

    private enum EffectType {
        CHOOSEANUMBER, CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT,
        CHOOSEPLAYERWITHCARDSINDECK, CHOOSETRIBBLEPOWER
    }
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");

        switch(effectType) {
            case CHOOSEANUMBER:
                environment.validateAllowedFields(effectObject, "amount", "memorize", "text");
                break;
            case CHOOSEPLAYEREXCEPT:
                environment.validateAllowedFields(effectObject, "memorize", "text", "exclude");
                break;
            case CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYERWITHCARDSINDECK, CHOOSETRIBBLEPOWER:
                environment.validateAllowedFields(effectObject, "memorize", "text");
                break;
        }
        environment.validateRequiredFields(effectObject, "memorize");



        final String memorize = effectObject.get("memorize").textValue();

        final String choiceText = environment.getString(effectObject, "text", getDefaultText(effectType));
        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("amount"), 0, environment);
        final PlayerSource excludePlayerSource =
                environment.getPlayerSource(effectObject, "exclude", true);


        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return switch (effectType) {
                    case CHOOSEANUMBER -> new PlayOutDecisionEffect(
                            new IntegerAwaitingDecision(context,1, context.substituteText(choiceText),
                                    valueSource.getMinimum(context),
                                    valueSource.getMaximum(context), null, memorize));
                    case CHOOSEOPPONENT -> new ChooseOpponentEffect(context, memorize);
                    case CHOOSEPLAYER -> new ChoosePlayerEffect(context, memorize);
                    case CHOOSEPLAYEREXCEPT ->
                            new ChoosePlayerExceptEffect(context, excludePlayerSource.getPlayerId(context), memorize);
                    case CHOOSEPLAYERWITHCARDSINDECK -> new ChoosePlayerWithCardsInDeckEffect(context, memorize);
                    case CHOOSETRIBBLEPOWER -> new ChooseTribblePowerEffect(context, memorize);
                };
            }
        };
    }

    private String getDefaultText(EffectType effectType) {
        return switch (effectType) {
            case CHOOSEANUMBER -> "Choose a number";
            case CHOOSEOPPONENT, CHOOSEPLAYER, CHOOSEPLAYEREXCEPT, CHOOSEPLAYERWITHCARDSINDECK -> "Choose a player";
            case CHOOSETRIBBLEPOWER -> "Choose a tribble power";
        };

    }

}