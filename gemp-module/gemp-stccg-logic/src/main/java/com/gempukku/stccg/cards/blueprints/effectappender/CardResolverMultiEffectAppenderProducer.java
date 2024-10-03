package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromZoneEffect;
import com.gempukku.stccg.actions.revealcards.RevealCardsFromYourHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
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
        DISCARD(null, false),
        DISCARDCARDSFROMDRAWDECK(Zone.DRAW_DECK, false),
        DISCARDFROMHAND(Zone.HAND, true),
        PUTCARDSFROMDECKINTOHAND(Zone.DRAW_DECK, false), // only premiere cards that will be reworded
        PUTCARDSFROMDECKONBOTTOMOFDECK(Zone.DRAW_DECK, false),
        PUTCARDSFROMDECKONTOPOFDECK(Zone.DRAW_DECK, false), // Celebratory Toast; Data, Keep Dealing
        PUTCARDSFROMDISCARDINTOHAND(Zone.DISCARD, false),
        PUTCARDSFROMDISCARDONBOTTOMOFDECK(Zone.DISCARD, false),
        PUTCARDSFROMDISCARDONTOPOFDECK(Zone.DISCARD, false), // Diplomatic Contact, For the Sisko
        PUTCARDSFROMHANDONBOTTOMOFDECK(Zone.HAND, false), // Recreation Room
        PUTCARDSFROMHANDONBOTTOMOFPLAYPILE(Zone.HAND, false),
        PUTCARDSFROMHANDONTOPOFDECK(Zone.HAND, true), // Bleed Resources
        REMOVEFROMTHEGAME(null, false),
        REMOVECARDSINDISCARDFROMGAME(Zone.DISCARD, false),
        REVEALCARDSFROMHAND(Zone.HAND, false),
        SHUFFLECARDSFROMDISCARDINTODRAWDECK(Zone.DISCARD, false), // Get It Done, Raktajino, Regenerate, Tinkerer
        SHUFFLECARDSFROMHANDINTODRAWDECK(Zone.HAND, false), // Isomagnetic Disintegrator
        SHUFFLECARDSFROMPLAYINTODRAWDECK(null, false); // Cloaked Maneuvers

        // From in play to bottom of deck - Ferengi Locator Bomb

        private final Zone fromZone;
        private final boolean shufflesCardsIntoDeck;
        private final boolean revealsCards;
        private final boolean showMatchingOnly;
        EffectType(Zone fromZone, boolean showMatchingOnly) {
            this.fromZone = fromZone;
            this.shufflesCardsIntoDeck = name().startsWith("SHUFFLECARDS") && name().endsWith("INTODRAWDECK");
            this.revealsCards = name().startsWith("REVEAL");
            this.showMatchingOnly = showMatchingOnly;
        }
        private String getZoneName() { return this.fromZone.getHumanReadable(); }
    }
    
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) 
            throws InvalidCardDefinitionException {

        // Get effectType from the JSON. Will throw an exception if the type isn't a valid EffectType.
        EffectType effectType = environment.getEnum(EffectType.class, effectObject, "type");

        /* Validate allowed fields in the JSON. An InvalidCardDefinitionException will be thrown if the JSON
            definition has any fields that the code does not expect. */
        validateAllowedFields(effectObject, environment, effectType);

        // Get blueprint parameters
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        final PlayerSource selectingPlayer = environment.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayer = environment.getTargetPlayerSource(effectObject);
        final String defaultText = getDefaultText(effectType);

        /* TODO - "reveal" indicates whether the card title will be visible in the chat. The default for this should
            be determined by the effect type, zone, player(s), and/or other parameters, not always true. */
        final boolean reveal = environment.getBoolean(effectObject, "reveal", true);
        final boolean forced = environment.getBoolean(effectObject, "forced", false);

        String filter = environment.getString(effectObject, "filter", "choose(any)");
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        final EffectAppender targetCardAppender;

        // TODO - choiceFilter only used for discard/remove
        FilterableSource choiceFilter = (actionContext) -> effectType == EffectType.DISCARD ?
                Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource()) :
                Filters.canBeRemovedFromTheGame;

        final String sourceMemory = filter.startsWith("memory(") ?
                filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")) : null;
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                        actionContext.getCardFromMemory(sourceMemory)).stream().toList();


        targetCardAppender = switch (effectType) {
            case DISCARD, REMOVEFROMTHEGAME ->
                    CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter, choiceFilter, count, memory,
                            selectingPlayer, defaultText, cardSource);
            case SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                    CardResolver.resolveCardsInPlay(filter, count, memory, selectingPlayer, defaultText, cardFilter);
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

                        if (effectType.shufflesCardsIntoDeck || effectType.revealsCards) {
                            // Don't break the shuffle or reveal effects into separate effects for each card
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
                                case REMOVEFROMTHEGAME ->
                                        new RemoveCardsFromTheGameEffect(context.getGame(),
                                                targetPlayer.getPlayerId(context), context.getSource(), cards);
                                case REMOVECARDSINDISCARDFROMGAME ->
                                        new RemoveCardsFromZoneEffect(context, cards, Zone.DISCARD);
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
            environment.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize");
            environment.validateRequiredFields(effectObject, "forced");
        } else {
            environment.validateAllowedFields(effectObject, "count", "filter", "reveal", "memorize");
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
            case REVEALCARDSFROMHAND -> "Choose cards to reveal";
            case SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK,
                    SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                    "Choose cards to shuffle into the draw deck";
        };
    }
}