package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlacePlayedCardBeneathDrawDeckEffect;
import com.gempukku.stccg.actions.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.actions.choose.ReorderTopCardsOfDeckEffect;
import com.gempukku.stccg.actions.discard.DiscardCardAtRandomFromHandEffect;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.actions.revealcards.LookAtOpponentsHandEffect;
import com.gempukku.stccg.actions.revealcards.LookAtRandomCardsFromHandEffect;
import com.gempukku.stccg.actions.revealcards.LookAtTopCardOfADeckEffect;
import com.gempukku.stccg.actions.revealcards.RevealBottomCardsOfDrawDeckEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class MiscEffectAppenderProducer implements EffectAppenderProducer {

    private enum EffectType {
        DISCARDCARDATRANDOMFROMHAND,
        DRAWCARDS,
        LOOKATHAND,
        LOOKATRANDOMCARDSFROMHAND,
        LOOKATTOPCARDSOFDRAWDECK,
        PLACEPLAYEDCARDBENEATHDRAWDECK,
        PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE,
        REORDERTOPCARDSOFDRAWDECK,
        REVEALBOTTOMCARDSOFDRAWDECK
    }
    
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        
        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");
        switch (effectType) {
            case DISCARDCARDATRANDOMFROMHAND:
                BlueprintUtils.validateAllowedFields(effectObject, "forced", "count", "memorize");
                environment.validateRequiredFields(effectObject, "forced");
                break;
            case DRAWCARDS, PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE, REORDERTOPCARDSOFDRAWDECK:
                BlueprintUtils.validateAllowedFields(effectObject, "count");
                break;
            case LOOKATHAND, PLACEPLAYEDCARDBENEATHDRAWDECK:
                BlueprintUtils.validateAllowedFields(effectObject);
                break;
            case LOOKATRANDOMCARDSFROMHAND, LOOKATTOPCARDSOFDRAWDECK, REVEALBOTTOMCARDSOFDRAWDECK:
                BlueprintUtils.validateAllowedFields(effectObject, "count", "memorize");
                break;
        }

        final String memory = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        final PlayerSource selectingPlayer = environment.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = environment.getTargetPlayerSource(effectObject);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = BlueprintUtils.getBoolean(effectObject, "forced", false);
        
        return new DefaultDelayedAppender() {
            @Override
            protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext context) {
                final String selectingPlayerId = selectingPlayer.getPlayerId(context);
                final String targetPlayerId = targetPlayer.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);
                List<Effect> result = new LinkedList<>();
                switch (effectType) {
                    case DISCARDCARDATRANDOMFROMHAND:
                        for (int i = 0; i < count; i++) 
                            result.add(new DiscardCardAtRandomFromHandEffect(context, targetPlayerId, forced));
                        break;
                    case DRAWCARDS:
                        result.add(new DrawCardsEffect(context.getGame(), action, targetPlayerId, count));
                        break;
                    case LOOKATHAND:
                        result.add(new LookAtOpponentsHandEffect(context, targetPlayer.getPlayerId(context)));
                        break;
                    case LOOKATRANDOMCARDSFROMHAND:
                        result.add(new LookAtRandomCardsFromHandEffect(context, targetPlayerId, count, memory));
                        break;
                    case LOOKATTOPCARDSOFDRAWDECK:
                        result.add(new LookAtTopCardOfADeckEffect(
                                context, count, targetPlayer.getPlayerId(context), memory));
                        break;
                    case PLACEPLAYEDCARDBENEATHDRAWDECK:
                        result.add(new PlacePlayedCardBeneathDrawDeckEffect(context));
                        break;
                    case PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE:
                        result.add(new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(context, targetPlayerId, count));
                        break;
                    case REORDERTOPCARDSOFDRAWDECK:
                        result.add(new ReorderTopCardsOfDeckEffect(context.getGame(), action, selectingPlayerId,
                                targetPlayerId, count));
                        break;
                    case REVEALBOTTOMCARDSOFDRAWDECK:
                        result.add(new RevealBottomCardsOfDrawDeckEffect(context, targetPlayerId, count, memory));
                }
                return result;
            }
            
            @Override
            public boolean isPlayableInFull(ActionContext context) {
                final int count = countSource.evaluateExpression(context, null);
                final String targetPlayerId = targetPlayer.getPlayerId(context);
                return switch (effectType) {
                    case DISCARDCARDATRANDOMFROMHAND -> {
                        final DefaultGame game = context.getGame();
                        yield context.getGameState().getHand(targetPlayerId).size() >= count
                                && (!forced || 
                                game.getModifiersQuerying().canDiscardCardsFromHand(targetPlayerId, context.getSource()));
                    }
                    case DRAWCARDS, LOOKATTOPCARDSOFDRAWDECK, PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE,
                            REORDERTOPCARDSOFDRAWDECK, REVEALBOTTOMCARDSOFDRAWDECK ->
                            context.getGameState().getDrawDeck(targetPlayerId).size() >= count;
                    case LOOKATHAND -> 
                            context.getPerformingPlayer()
                                    .canLookOrRevealCardsInHandOfPlayer(targetPlayer.getPlayerId(context));
                    case LOOKATRANDOMCARDSFROMHAND -> 
                            context.getPerformingPlayer().canLookOrRevealCardsInHandOfPlayer(targetPlayerId) &&
                            context.getGameState().getHand(targetPlayerId).size() >= count;
                    case PLACEPLAYEDCARDBENEATHDRAWDECK -> true;
                };
            }
        };
    }
}