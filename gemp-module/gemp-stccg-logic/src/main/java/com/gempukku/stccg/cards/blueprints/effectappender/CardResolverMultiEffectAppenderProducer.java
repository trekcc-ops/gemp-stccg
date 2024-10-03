package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolverMultiEffectAppenderProducer implements EffectAppenderProducer {

    // TODO - "shuffleAfter" specifically refers to the "shuffle" property in JSON. Some of the effects called also shuffle.
    // Don't rename these without renaming the corresponding JSON "type" property
    private enum EffectType { DISCARD(false, null),
        DISCARDCARDSFROMDRAWDECK(false, Zone.DRAW_DECK),
        PUTCARDSFROMDECKINTOHAND(true, Zone.DRAW_DECK),
        PUTCARDSFROMDECKONBOTTOMOFDECK(false, Zone.DRAW_DECK),
        PUTCARDSFROMDECKONTOPOFDECK(false, Zone.DRAW_DECK),
        PUTCARDSFROMDISCARDONTOPOFDECK(false, Zone.DISCARD),
        PUTCARDSFROMHANDONTOPOFDECK(false, Zone.HAND),
        REMOVEFROMTHEGAME(false, null),
        REMOVECARDSINDISCARDFROMGAME(false, Zone.DISCARD),
        SHUFFLECARDSFROMDISCARDINTODRAWDECK(false, Zone.DISCARD),
        SHUFFLECARDSFROMHANDINTODRAWDECK(false, Zone.HAND),
        SHUFFLECARDSFROMPLAYINTODRAWDECK(false, null);

        private final boolean shuffleAfter;
        private final Zone fromZone;
        EffectType(boolean shuffleAfter, Zone fromZone) {
            this.shuffleAfter = shuffleAfter;
            this.fromZone = fromZone;
        }
        private boolean getShuffleAfter() { return this.shuffleAfter; }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }

    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {

        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");
        switch(effectType) {
            case DISCARD, PUTCARDSFROMDISCARDONTOPOFDECK, REMOVEFROMTHEGAME, REMOVECARDSINDISCARDFROMGAME, SHUFFLECARDSFROMDISCARDINTODRAWDECK,
                    SHUFFLECARDSFROMHANDINTODRAWDECK, SHUFFLECARDSFROMPLAYINTODRAWDECK:
                environment.validateAllowedFields(effectObject, "player", "count", "filter", "memorize");
                break;
            case DISCARDCARDSFROMDRAWDECK:
                environment.validateAllowedFields(effectObject, "selectingPlayer", "targetPlayer", "count", "filter", "memorize");
                break;
            case PUTCARDSFROMDECKONBOTTOMOFDECK, PUTCARDSFROMDECKONTOPOFDECK:
                environment.validateAllowedFields(effectObject, "count", "filter", "reveal");
                break;
            case PUTCARDSFROMHANDONTOPOFDECK:
                environment.validateAllowedFields(effectObject, "selectingPlayer", "targetPlayer", "optional", "filter", "count", "reveal", "memorize");
                break;
            case PUTCARDSFROMDECKINTOHAND:
                environment.validateAllowedFields(effectObject, "count", "filter", "shuffle", "reveal");
                break;
        }

        MultiEffectAppender result = new MultiEffectAppender();
        final EffectAppender targetCardAppender;
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);
        final boolean reveal = environment.getBoolean(effectObject, "reveal", true); // TODO - should be determined by the zone
        final boolean shuffle = environment.getBoolean(effectObject, "shuffle", effectType.getShuffleAfter());

        targetCardAppender = switch (effectType) {
            case DISCARD, REMOVEFROMTHEGAME ->
                    getTargetCardAppender(effectObject, environment, effectType, memory, playerSource);
            case PUTCARDSFROMHANDONTOPOFDECK ->
                    environment.buildTargetCardAppender(effectObject, "Choose cards from " + effectType.getZoneName(), effectType.fromZone, memory, true);
            case PUTCARDSFROMDISCARDONTOPOFDECK, REMOVECARDSINDISCARDFROMGAME, PUTCARDSFROMDECKONBOTTOMOFDECK,
                    PUTCARDSFROMDECKONTOPOFDECK, PUTCARDSFROMDECKINTOHAND ->
                    environment.buildTargetCardAppender(effectObject, "Choose cards from " + effectType.getZoneName(), effectType.fromZone, memory);
            case DISCARDCARDSFROMDRAWDECK ->
                    environment.buildTargetCardAppender(effectObject, "Choose cards to discard", Zone.DRAW_DECK, memory);
            case SHUFFLECARDSFROMDISCARDINTODRAWDECK ->
                    environment.buildTargetCardAppender(effectObject, playerSource, "Choose cards to shuffle in", Zone.DISCARD, memory);
            case SHUFFLECARDSFROMHANDINTODRAWDECK ->
                    environment.buildTargetCardAppender(effectObject, "Choose cards to shuffle into the draw deck", Zone.HAND, memory);
            case SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                CardResolver.resolveCardsInPlay(environment.getString(effectObject, "filter", "choose(any)"),
                        ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment), memory,
                        playerSource, "Choose cards to shuffle into your deck",
                        environment.getCardFilterableIfChooseOrAll(environment.getString(effectObject, "filter", "choose(any)")));
        };

        result.addEffectAppender(targetCardAppender);

        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        final List<Collection<PhysicalCard>> effectCardLists = new LinkedList<>();
                        switch(effectType) {
                            case SHUFFLECARDSFROMPLAYINTODRAWDECK, SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK:
                                // Don't break the shuffle effects into separate effects for each card
                                effectCardLists.add(cardsFromMemory);
                                break;
                            default:
                                for (PhysicalCard card : cardsFromMemory) {
                                    effectCardLists.add(Collections.singletonList(card));
                                }
                                break;
                        }

                        List<Effect> effects = new LinkedList<>();
                        for (Collection<PhysicalCard> cards : effectCardLists) {
                            Effect effect = switch (effectType) {
                                case DISCARD ->
                                        new DiscardCardsFromPlayEffect(context, playerSource.getPlayerId(context), Iterables.getOnlyElement(cards));
                                case DISCARDCARDSFROMDRAWDECK ->
                                        new DiscardCardsFromZoneEffect(context.getGame(), action.getActionSource(), Zone.DRAW_DECK, cards);
                                case PUTCARDSFROMDECKINTOHAND ->
                                        new PutCardFromZoneIntoHandEffect(context.getGame(), Iterables.getOnlyElement(cards), Zone.DRAW_DECK, reveal);
                                case PUTCARDSFROMDECKONBOTTOMOFDECK ->
                                        new PutCardsFromZoneOnEndOfPileEffect(context.getGame(), reveal, effectType.fromZone, Zone.DRAW_DECK, EndOfPile.BOTTOM, Iterables.getOnlyElement(cards));
                                case PUTCARDSFROMDECKONTOPOFDECK, PUTCARDSFROMDISCARDONTOPOFDECK, PUTCARDSFROMHANDONTOPOFDECK ->
                                        new PutCardsFromZoneOnEndOfPileEffect(context.getGame(), reveal, effectType.fromZone, Zone.DRAW_DECK, EndOfPile.TOP, Iterables.getOnlyElement(cards));
                                case REMOVEFROMTHEGAME ->
                                        new RemoveCardsFromTheGameEffect(context.getGame(), playerSource.getPlayerId(context), context.getSource(), cards);
                                case REMOVECARDSINDISCARDFROMGAME ->
                                        new RemoveCardsFromZoneEffect(context, cards, Zone.DISCARD);
                                case SHUFFLECARDSFROMDISCARDINTODRAWDECK ->
                                        new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(), Zone.DISCARD, playerSource.getPlayerId(context), cards);
                                case SHUFFLECARDSFROMHANDINTODRAWDECK ->
                                        new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(), Zone.HAND, playerSource.getPlayerId(context), cards);
                                case SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                                        new ShuffleCardsFromPlayIntoDeckEffect(context, playerSource.getPlayerId(context), cards);
                            };
                            effects.add(effect);
                        }
                        return effects;
                    }
                });

        if (shuffle)
            result.addEffectAppender(
                    new DefaultDelayedAppender() {
                        @Override
                        protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                            return new ShuffleDeckEffect(context.getGame(), context.getPerformingPlayerId());
                        }
                    });

        return result;
    }

    private EffectAppender getTargetCardAppender(JsonNode effectObject, CardBlueprintFactory environment, EffectType effectType,
                                                 String memory, PlayerSource player) throws InvalidCardDefinitionException {
        // Works for discard and removefromthegame only
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = effectObject.get("filter").textValue();

        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        final String sourceMemory = filter.startsWith("memory(") ? filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")) : null;
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                        actionContext.getCardFromMemory(sourceMemory)).stream().toList();

        FilterableSource choiceFilter = (actionContext) -> effectType == EffectType.DISCARD ?
                Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource()) :
                Filters.canBeRemovedFromTheGame;

        String choiceText = "Choose cards to " +
                (effectType == EffectType.DISCARD ? "discard" : "remove from the game");

        return CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter,
                choiceFilter, valueSource, memory, player, "Choose cards to " + choiceText,
                cardSource);

    }
}