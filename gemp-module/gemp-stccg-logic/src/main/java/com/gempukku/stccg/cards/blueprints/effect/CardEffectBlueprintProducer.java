package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.discard.DiscardCardAtRandomFromHandEffect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.actions.revealcards.*;
import com.gempukku.stccg.actions.turn.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifiersQuerying;

import java.util.LinkedList;
import java.util.List;

public class CardEffectBlueprintProducer {

    private enum EffectType {
        DISCARDBOTTOMCARDFROMDECK, DISCARDCARDATRANDOMFROMHAND, DISCARDTOPCARDSFROMDECK, DISCARDTOPCARDFROMPLAYPILE,
        DRAWCARDS, LOOKATHAND, LOOKATRANDOMCARDSFROMHAND, LOOKATTOPCARDSOFDRAWDECK,
        PLACEPLAYEDCARDBENEATHDRAWDECK, PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE,
        REVEALBOTTOMCARDSOFDRAWDECK, REVEALHAND
    }
    
    public static EffectBlueprint createEffectBlueprint(JsonNode effectObject)
            throws InvalidCardDefinitionException {

        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");

        String[] allowedFields = switch (effectType) {
            case DISCARDBOTTOMCARDFROMDECK, DISCARDCARDATRANDOMFROMHAND, DISCARDTOPCARDSFROMDECK ->
                    new String[]{"forced", "count", "memorize"};
            case DRAWCARDS, PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE-> new String[]{"count"};
            case LOOKATHAND, PLACEPLAYEDCARDBENEATHDRAWDECK -> new String[]{};
            case DISCARDTOPCARDFROMPLAYPILE, LOOKATRANDOMCARDSFROMHAND, LOOKATTOPCARDSOFDRAWDECK,
                    REVEALBOTTOMCARDSOFDRAWDECK ->
                    new String[]{"count", "memorize"};
            case REVEALHAND -> new String[]{"memorize"};
        };
        BlueprintUtils.validateAllowedFields(effectObject, allowedFields);

        if (effectType == EffectType.DISCARDCARDATRANDOMFROMHAND || effectType == EffectType.DISCARDBOTTOMCARDFROMDECK
                || effectType == EffectType.DISCARDTOPCARDSFROMDECK)
            BlueprintUtils.validateRequiredFields(effectObject, "forced");

        final String memory = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        final PlayerSource selectingPlayer = BlueprintUtils.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayerSource = BlueprintUtils.getTargetPlayerSource(effectObject);
        final ValueSource countSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1);
        final boolean forced = BlueprintUtils.getBoolean(effectObject, "forced", false);
        
        return new DelayedEffectBlueprint() {
            @Override
            protected List<Action> createActions(Action action, ActionContext context) {
                final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                DefaultGame cardGame = context.getGame();
                Player targetPlayer = cardGame.getPlayer(targetPlayerId);
                final int count = countSource.evaluateExpression(context, null);
                List<Action> result = new LinkedList<>();
                int numberOfEffects = (effectType == EffectType.DISCARDCARDATRANDOMFROMHAND) ? count : 1;
                for (int i = 0; i < numberOfEffects; i++) {
                    Effect effect = switch (effectType) {
                        case DISCARDBOTTOMCARDFROMDECK -> new DiscardCardsFromEndOfCardPileEffect(Zone.DRAW_DECK,
                                EndOfPile.BOTTOM, targetPlayerId, count, forced, context, memory);
                        case DISCARDCARDATRANDOMFROMHAND ->
                                new DiscardCardAtRandomFromHandEffect(context, targetPlayerId, forced);
                        case DISCARDTOPCARDSFROMDECK ->
                                new DiscardCardsFromEndOfCardPileEffect(Zone.DRAW_DECK, EndOfPile.TOP, targetPlayerId,
                                        count, forced, context, memory);
                        case DISCARDTOPCARDFROMPLAYPILE ->
                                new DiscardCardsFromEndOfCardPileEffect(Zone.PLAY_PILE, EndOfPile.TOP, targetPlayerId,
                                        count, true, context, memory);
                        case DRAWCARDS -> new StackActionEffect(context.getGame(),
                                new DrawCardAction(context.getSource(), targetPlayer, count));
                        case LOOKATHAND -> new LookAtOpponentsHandEffect(context, targetPlayerId);
                        case LOOKATRANDOMCARDSFROMHAND ->
                                new LookAtRandomCardsFromHandEffect(context, targetPlayerId, count, memory);
                        case LOOKATTOPCARDSOFDRAWDECK ->
                                new LookAtTopCardOfADeckEffect(context, count, targetPlayerId, memory);
                        case PLACEPLAYEDCARDBENEATHDRAWDECK ->
                                new StackActionEffect(cardGame, new PlaceCardsOnBottomOfDrawDeckAction(targetPlayer,
                                        List.of(context.getSource()), context.getSource()));
                        case PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE ->
                                new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(context, targetPlayerId, count);
                        case REVEALBOTTOMCARDSOFDRAWDECK ->
                                new RevealBottomCardsOfDrawDeckEffect(context, targetPlayerId, count, memory);
                        case REVEALHAND -> new RevealHandEffect(context, targetPlayerId, memory);
                    };
                    result.add(new SubAction(action, effect));
                }
                return result;
            }
            
            @Override
            public boolean isPlayableInFull(ActionContext context) {
                final int count = countSource.evaluateExpression(context, null);
                final String targetPlayerId = targetPlayerSource.getPlayerId(context);
                final DefaultGame game = context.getGame();
                final PhysicalCard source = context.getSource();
                final ModifiersQuerying modifiersQuerying = game.getModifiersQuerying();
                return switch (effectType) {
                    case DISCARDBOTTOMCARDFROMDECK, DRAWCARDS, LOOKATTOPCARDSOFDRAWDECK,
                            PLACETOPCARDOFDRAWDECKONTOPOFPLAYPILE, REVEALBOTTOMCARDSOFDRAWDECK ->
                            context.getGameState().getDrawDeck(targetPlayerId).size() >= count;
                    case DISCARDCARDATRANDOMFROMHAND ->
                            context.getGameState().getHand(targetPlayerId).size() >= count &&
                                    (!forced || modifiersQuerying.canDiscardCardsFromHand(targetPlayerId, source));
                    case DISCARDTOPCARDSFROMDECK ->
                            context.getGameState().getDrawDeck(targetPlayerId).size() >= count &&
                                    (!forced || modifiersQuerying
                                            .canDiscardCardsFromTopOfDeck(context.getPerformingPlayerId(), source));
                    case DISCARDTOPCARDFROMPLAYPILE ->
                            context.getGameState().getZoneCards(targetPlayerId, Zone.PLAY_PILE).size() >= count;
                    case LOOKATHAND, REVEALHAND ->
                            context.getPerformingPlayer().canLookOrRevealCardsInHandOfPlayer(targetPlayerId);
                    case LOOKATRANDOMCARDSFROMHAND -> 
                            context.getPerformingPlayer().canLookOrRevealCardsInHandOfPlayer(targetPlayerId) &&
                                    context.getGameState().getHand(targetPlayerId).size() >= count;
                    case PLACEPLAYEDCARDBENEATHDRAWDECK -> true;
                };
            }
        };
    }
}