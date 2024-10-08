package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.actions.revealcards.RevealCardEffect;
import com.gempukku.stccg.actions.revealcards.RevealCardsFromYourHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolverMultiEffectAppenderProducer implements EffectAppenderProducer {

    // Don't rename these without renaming the corresponding JSON "type" property
    private enum EffectType { 
        DISCARD(null, false, true),
        DISCARDCARDSFROMDRAWDECK(Zone.DRAW_DECK, false, true),
        DISCARDFROMHAND(Zone.HAND, true, true),
        PUTCARDSFROMDECKINTOHAND(Zone.DRAW_DECK, false, true), // only premiere cards that will be reworded
        PUTCARDSFROMDECKONBOTTOMOFDECK(Zone.DRAW_DECK, false, true),
        PUTCARDSFROMDECKONTOPOFDECK(Zone.DRAW_DECK, false, true), // Celebratory Toast; Data, Keep Dealing
        PUTCARDSFROMDISCARDINTOHAND(Zone.DISCARD, false, true),
        PUTCARDSFROMDISCARDONBOTTOMOFDECK(Zone.DISCARD, false, true),
        PUTCARDSFROMDISCARDONTOPOFDECK(Zone.DISCARD, false, true), // Diplomatic Contact, For the Sisko
        PUTCARDSFROMHANDONBOTTOMOFDECK(Zone.HAND, false, true), // Recreation Room
        PUTCARDSFROMHANDONBOTTOMOFPLAYPILE(Zone.HAND, false, true),
        PUTCARDSFROMHANDONTOPOFDECK(Zone.HAND, true, true), // Bleed Resources
        PUTCARDSFROMPLAYONBOTTOMOFDECK(null, false, true), // Ferengi Locator Bomb
        REMOVEFROMTHEGAME(null, false, true),
        REMOVECARDSINDISCARDFROMGAME(Zone.DISCARD, false, true),
        RETURNTOHAND(null, false, false),
        REVEALCARDS(null, false, false),
        REVEALCARDSFROMHAND(Zone.HAND, false, false),
        SHUFFLECARDSFROMDISCARDINTODRAWDECK(Zone.DISCARD, false, false), // Get It Done, Raktajino, Regenerate, Tinkerer
        SHUFFLECARDSFROMHANDINTODRAWDECK(Zone.HAND, false, false), // Isomagnetic Disintegrator
        SHUFFLECARDSFROMPLAYINTODRAWDECK(null, false, false); // Cloaked Maneuvers

        private final Zone fromZone;
        private final boolean showMatchingOnly;
        private final boolean separateEffects;
        EffectType(Zone fromZone, boolean showMatchingOnly, boolean separateEffects) {
            this.fromZone = fromZone;
            this.showMatchingOnly = showMatchingOnly;
            this.separateEffects = separateEffects;
        }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }
    
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        // Get effectType from the JSON. Will throw an exception if the type isn't a valid EffectType.
        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");

        /* Validate allowed fields in the JSON. An InvalidCardDefinitionException will be thrown if the JSON
            definition has any fields that the code does not expect. */
        validateAllowedFields(effectObject, environment, effectType);

        // Get blueprint parameters
        final String memory = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        final PlayerSource selectingPlayer = environment.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = environment.getTargetPlayerSource(effectObject);
        final String defaultText = getDefaultText(effectType);

        /* TODO - "reveal" indicates whether the card title will be visible in the chat. The default for this should
            be determined by the effect type, zone, player(s), and/or other parameters, not always true. */
        final boolean reveal = BlueprintUtils.getBoolean(effectObject, "reveal", true);
        final boolean forced = BlueprintUtils.getBoolean(effectObject, "forced", false);

        String filter = BlueprintUtils.getString(effectObject, "filter", "choose(any)");
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectBlueprint result = new MultiEffectBlueprint();
        final EffectBlueprint targetCardAppender;

        // TODO - choiceFilter only used for discard/remove/return to hand
        FilterableSource choiceFilter = (actionContext) -> {
            if (effectType == EffectType.DISCARD)
                return Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource());
            else if (effectType == EffectType.REMOVEFROMTHEGAME)
                return Filters.canBeRemovedFromTheGame;
            else if (effectType == EffectType.RETURNTOHAND)
                return (Filter) (game, physicalCard) ->
                        game.getModifiersQuerying().canBeReturnedToHand(physicalCard, actionContext.getSource());
            return null;
        };

        final String sourceMemory = filter.startsWith("memory(") ?
                filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")) : null;
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                        actionContext.getCardFromMemory(sourceMemory)).stream().toList();


        targetCardAppender = switch (effectType) {
            case DISCARD, REMOVEFROMTHEGAME ->
                    CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter, choiceFilter, count, memory,
                            selectingPlayer, defaultText, cardSource);
            case PUTCARDSFROMPLAYONBOTTOMOFDECK, SHUFFLECARDSFROMPLAYINTODRAWDECK, REVEALCARDS ->
                    CardResolver.resolveCardsInPlay(filter, count, memory, selectingPlayer, defaultText, cardFilter);
            case RETURNTOHAND ->
                    CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter, choiceFilter, count, memory,
                            targetPlayer, defaultText, cardSource);
            case DISCARDFROMHAND, PUTCARDSFROMHANDONTOPOFDECK, DISCARDCARDSFROMDRAWDECK, PUTCARDSFROMDECKINTOHAND,
                    PUTCARDSFROMDECKONBOTTOMOFDECK, PUTCARDSFROMDECKONTOPOFDECK, PUTCARDSFROMDISCARDINTOHAND,
                    PUTCARDSFROMDISCARDONBOTTOMOFDECK, PUTCARDSFROMDISCARDONTOPOFDECK, PUTCARDSFROMHANDONBOTTOMOFDECK,
                    PUTCARDSFROMHANDONBOTTOMOFPLAYPILE, REMOVECARDSINDISCARDFROMGAME, REVEALCARDSFROMHAND,
                    SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK ->
                    CardResolver.resolveCardsInZone(filter, null, count, memory, selectingPlayer,
                            targetPlayer, defaultText, cardFilter, effectType.fromZone, effectType.showMatchingOnly,
                            environment.getCardSourceFromZone(targetPlayer, effectType.fromZone, filter));
        };

        result.addEffectAppender(targetCardAppender);

        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, 
                                                         ActionContext context) {
                        final Collection<PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        final List<Collection<PhysicalCard>> effectCardLists = new LinkedList<>();

                        if (!effectType.separateEffects) {
                            // For some effect types, all cards should be moved in a single action
                            effectCardLists.add(cardsFromMemory);
                        } else {
                            for (PhysicalCard card : cardsFromMemory) {
                                effectCardLists.add(Collections.singletonList(card));
                            }
                        }
                        
                        List<Effect> effects = new LinkedList<>();
                        for (Collection<PhysicalCard> cards : effectCardLists) {
                            Effect effect = switch (effectType) {
                                case DISCARD ->
                                        new DiscardCardsFromPlayEffect(context, targetPlayer.getPlayerId(context),
                                                Iterables.getOnlyElement(cards));
                                case DISCARDCARDSFROMDRAWDECK ->
                                        new DiscardCardsFromZoneEffect(context.getGame(), action.getActionSource(),
                                                Zone.DRAW_DECK, cards);
                                case DISCARDFROMHAND ->
                                        new DiscardCardsFromZoneEffect(context, Zone.HAND, 
                                                targetPlayer.getPlayerId(context), cards, forced);
                                case PUTCARDSFROMDECKINTOHAND ->
                                        new PutCardFromZoneIntoHandEffect(context.getGame(),
                                                Iterables.getOnlyElement(cards), effectType.fromZone, reveal);
                                case PUTCARDSFROMDECKONBOTTOMOFDECK, PUTCARDSFROMDISCARDONBOTTOMOFDECK,
                                        PUTCARDSFROMHANDONBOTTOMOFDECK ->
                                        new PutCardsFromZoneOnEndOfPileEffect(context.getGame(), reveal,
                                                effectType.fromZone, Zone.DRAW_DECK, EndOfPile.BOTTOM,
                                                Iterables.getOnlyElement(cards));
                                case PUTCARDSFROMHANDONBOTTOMOFPLAYPILE ->
                                        new PutCardsFromZoneOnEndOfPileEffect(context.getGame(), reveal,
                                                effectType.fromZone, Zone.PLAY_PILE, EndOfPile.BOTTOM,
                                                Iterables.getOnlyElement(cards));
                                case PUTCARDSFROMDISCARDINTOHAND ->
                                        new PutCardFromZoneIntoHandEffect(context.getGame(),
                                                Iterables.getOnlyElement(cards), effectType.fromZone);
                                case PUTCARDSFROMDECKONTOPOFDECK, PUTCARDSFROMDISCARDONTOPOFDECK,
                                        PUTCARDSFROMHANDONTOPOFDECK ->
                                        new PutCardsFromZoneOnEndOfPileEffect(context.getGame(), reveal,
                                                effectType.fromZone, Zone.DRAW_DECK, EndOfPile.TOP,
                                                Iterables.getOnlyElement(cards));
                                case PUTCARDSFROMPLAYONBOTTOMOFDECK ->
                                        new PutCardFromPlayOnBottomOfDeckEffect(Iterables.getOnlyElement(cards));
                                case REMOVEFROMTHEGAME ->
                                        new RemoveCardsFromTheGameEffect(context.getGame(),
                                                targetPlayer.getPlayerId(context), context.getSource(), cards);
                                case REMOVECARDSINDISCARDFROMGAME ->
                                        new RemoveCardsFromZoneEffect(context, cards, Zone.DISCARD);
                                case RETURNTOHAND ->
                                        new ReturnCardsToHandEffect(
                                                context.getGame(), context.getSource(), Filters.in(cards));
                                case REVEALCARDS -> new RevealCardEffect(context, cards);
                                case REVEALCARDSFROMHAND ->
                                        new RevealCardsFromYourHandEffect(context, cards);
                                case SHUFFLECARDSFROMDISCARDINTODRAWDECK ->
                                        new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(),
                                                Zone.DISCARD, targetPlayer.getPlayerId(context), cards);
                                case SHUFFLECARDSFROMHANDINTODRAWDECK ->
                                        new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(),
                                                Zone.HAND, targetPlayer.getPlayerId(context), cards);
                                case SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                                        new ShuffleCardsFromPlayIntoDeckEffect(
                                                context, targetPlayer.getPlayerId(context), cards);
                            };
                            effects.add(effect);
                        }
                        return effects;
                    }
                    
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        if (effectType == EffectType.DISCARDFROMHAND) {
                            final ModifiersQuerying modifiers = actionContext.getGame().getModifiersQuerying();
                            final String handPlayer = targetPlayer.getPlayerId(actionContext);
                            final String choosingPlayer = selectingPlayer.getPlayerId(actionContext);
                            if (!handPlayer.equals(choosingPlayer) &&
                                    modifiers.canLookOrRevealCardsInHand(handPlayer, choosingPlayer))
                                return false;
                            return (!forced || modifiers.canDiscardCardsFromHand(handPlayer, actionContext.getSource()));
                        } else return super.isPlayableInFull(actionContext);
                    }
                });
        return result;
    }

    private void validateAllowedFields(JsonNode effectObject, CardBlueprintFactory environment, EffectType effectType)
            throws InvalidCardDefinitionException {
        if (effectType == EffectType.DISCARDFROMHAND) {
            BlueprintUtils.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize");
            environment.validateRequiredFields(effectObject, "forced");
        } else if (effectType == EffectType.RETURNTOHAND) {
            BlueprintUtils.validateAllowedFields(effectObject, "filter", "count");
            environment.validateRequiredFields(effectObject, "filter");
        }else {
            BlueprintUtils.validateAllowedFields(effectObject, "count", "filter", "reveal", "memorize");
        }
    }

    private String getDefaultText(EffectType effectType) {
        return switch (effectType) {
            case DISCARD, DISCARDCARDSFROMDRAWDECK -> "Choose cards to discard";
            case DISCARDFROMHAND -> "Choose cards from hand to discard";
            case PUTCARDSFROMDISCARDONBOTTOMOFDECK, PUTCARDSFROMDISCARDONTOPOFDECK, PUTCARDSFROMHANDONTOPOFDECK,
                    REMOVECARDSINDISCARDFROMGAME, PUTCARDSFROMDECKONBOTTOMOFDECK, PUTCARDSFROMDECKONTOPOFDECK,
                    PUTCARDSFROMDECKINTOHAND, PUTCARDSFROMDISCARDINTOHAND ->
                    "Choose cards from " + effectType.getZoneName();
            case PUTCARDSFROMHANDONBOTTOMOFDECK -> "Choose cards from hand to put beneath draw deck";
            case PUTCARDSFROMHANDONBOTTOMOFPLAYPILE -> "Choose cards from hand to put beneath play pile";
            case REMOVEFROMTHEGAME -> "Choose cards to remove from the game";
            case REVEALCARDS, REVEALCARDSFROMHAND -> "Choose cards to reveal";
            case SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK,
                    SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                    "Choose cards to shuffle into the draw deck";
            case RETURNTOHAND -> "Choose cards to return to hand";
            case PUTCARDSFROMPLAYONBOTTOMOFDECK ->  "Choose cards in play";
        };
    }
}