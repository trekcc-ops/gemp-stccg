package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.AppendableAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.discard.RemoveCardFromPlayAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardResolverMultiEffectBlueprintProducer {

    // Don't rename these without renaming the corresponding JSON "type" property
    private enum EffectType {
        DISCARD(null, false, true),
        DISCARDCARDSFROMDRAWDECK(Zone.DRAW_DECK, false, true),
        DISCARDFROMHAND(Zone.HAND, true, true),
        DOWNLOAD(Zone.DRAW_DECK, false, false), // TODO - Should allow downloading from more than one zone
        PUTCARDSFROMPLAYONBOTTOMOFDECK(null, false, true), // Ferengi Locator Bomb
        REMOVECARDSINDISCARDFROMGAME(Zone.DISCARD, false, true),
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

    public static EffectBlueprint createEffectBlueprint(JsonNode effectObject)
            throws InvalidCardDefinitionException {

        // Get effectType from the JSON. Will throw an exception if the type isn't a valid EffectType.
        EffectType effectType = BlueprintUtils.getEnum(EffectType.class, effectObject, "type");

        /* Validate allowed fields in the JSON. An InvalidCardDefinitionException will be thrown if the JSON
            definition has any fields that the code does not expect. */
        validateAllowedFields(effectObject, effectType);

        // Get blueprint parameters
        final String memory = BlueprintUtils.getString(effectObject, "memorize", "_temp");
        final PlayerSource selectingPlayer = BlueprintUtils.getSelectingPlayerSource(effectObject);
        final PlayerSource targetPlayerSource = BlueprintUtils.getTargetPlayerSource(effectObject);
        final String defaultText = getDefaultText(effectType);

        /* TODO - "reveal" indicates whether the card title will be visible in the chat. The default for this should
            be determined by the effect type, zone, player(s), and/or other parameters, not always true. */
        final boolean reveal = BlueprintUtils.getBoolean(effectObject, "reveal", true);
        final boolean forced = BlueprintUtils.getBoolean(effectObject, "forced", false);

        String filter = BlueprintUtils.getString(effectObject, "filter", "choose(any)");
        FilterableSource cardFilter = (filter.startsWith("all(") || filter.startsWith("choose(")) ?
                new FilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")"))) :
                null;

        String onFilter = effectObject.has("on") ? effectObject.get("on").textValue() : null;
        final FilterableSource onFilterableSource = (onFilter != null) ? new FilterFactory().generateFilter(onFilter) : null;

        ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1);

        MultiEffectBlueprint result = new MultiEffectBlueprint();

        if (effectType == EffectType.DOWNLOAD) {
            result.setPlayabilityCheckedForEffect(true);
        }

        final EffectBlueprint targetCardAppender;

        FilterableSource choiceFilter = (actionContext) -> switch (effectType) {
            case DISCARD ->
                    Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource());
            case DOWNLOAD -> Filters.playable;
            default -> null;
        };

        Function<ActionContext, List<PhysicalCard>> cardSource =
                getCardSource(filter, effectType.fromZone, targetPlayerSource);

        targetCardAppender = switch (effectType) {
            case DISCARD, PUTCARDSFROMPLAYONBOTTOMOFDECK, SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                    CardResolver.resolveCardsInPlay(filter, cardFilter, choiceFilter, choiceFilter, count, memory,
                            selectingPlayer, defaultText, cardSource);
            case DOWNLOAD, DISCARDFROMHAND, DISCARDCARDSFROMDRAWDECK, REMOVECARDSINDISCARDFROMGAME,
                    SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK ->
                    CardResolver.resolveCardsInZone(filter, choiceFilter, count, memory,
                            selectingPlayer, targetPlayerSource, defaultText, cardFilter, effectType.fromZone,
                            cardSource);
        };

        result.addEffectBlueprint(targetCardAppender);

        result.addEffectBlueprint(
                new DelayedEffectBlueprint() {
                    @Override
                    protected List<Action> createActions(AppendableAction parentAction, ActionContext context) {
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
                        
                        List<Action> subActions = new LinkedList<>();
                        String targetPlayerId = targetPlayerSource.getPlayerId(context);
                        Player targetPlayer = context.getGame().getPlayer(targetPlayerId);
                        for (Collection<PhysicalCard> cards : effectCardLists) {
                            Action subAction = switch (effectType) {
                                case DISCARD, DISCARDFROMHAND ->
                                        new DiscardCardAction(context.getSource(), targetPlayer, cards);
                                case DISCARDCARDSFROMDRAWDECK ->
                                        new DiscardCardAction(context.getSource(),
                                                targetPlayer, Iterables.getOnlyElement(cards));
                                case DOWNLOAD -> Iterables.getOnlyElement(cards).getPlayCardAction(true);
                                case PUTCARDSFROMPLAYONBOTTOMOFDECK ->
                                        new PlaceCardsOnBottomOfDrawDeckAction(targetPlayer, cards, context.getSource());
                                case REMOVECARDSINDISCARDFROMGAME ->
                                        new RemoveCardFromPlayAction(targetPlayer, context.getSource(), cards);
                                case SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK,
                                        SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                                        new ShuffleCardsIntoDrawDeckAction(context.getSource(), context.getPerformingPlayer(), Filters.in(cards));
                            };
                            subActions.add(subAction);
                        }
                        return subActions;
                    }
                    
                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        if (effectType == EffectType.DISCARDFROMHAND) {
                            final ModifiersQuerying modifiers = actionContext.getGame().getModifiersQuerying();
                            final String handPlayer = targetPlayerSource.getPlayerId(actionContext);
                            final String choosingPlayer = selectingPlayer.getPlayerId(actionContext);
                            if (!handPlayer.equals(choosingPlayer) &&
                                    modifiers.canLookOrRevealCardsInHand(handPlayer, choosingPlayer))
                                return false;
                            return (!forced || modifiers.canDiscardCardsFromHand(handPlayer, actionContext.getSource()));
                        } else if (effectType == EffectType.DOWNLOAD) {
                            return !actionContext.getGame().getModifiersQuerying().hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
                        } else return super.isPlayableInFull(actionContext);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        if (effectType == EffectType.DOWNLOAD)
                            return true;
                        else return super.isPlayabilityCheckedForEffect();
                    }
                });
        return result;
    }

    private static void validateAllowedFields(JsonNode effectObject, EffectType effectType)
            throws InvalidCardDefinitionException {
        if (effectType == EffectType.DISCARDFROMHAND) {
            BlueprintUtils.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize");
            BlueprintUtils.validateRequiredFields(effectObject, "forced");
        } else if (effectType == EffectType.DOWNLOAD) {
            BlueprintUtils.validateAllowedFields(effectObject, "filter", "memorize");
            BlueprintUtils.validateRequiredFields(effectObject, "filter");
        } else {
            BlueprintUtils.validateAllowedFields(effectObject, "count", "filter", "reveal", "memorize");
        }
    }

    private static String getDefaultText(EffectType effectType) {
        return switch (effectType) {
            case DISCARD, DISCARDCARDSFROMDRAWDECK -> "Choose cards to discard";
            case DISCARDFROMHAND -> "Choose cards from hand to discard";
            case DOWNLOAD -> "Choose card to play";
            case REMOVECARDSINDISCARDFROMGAME ->
                    "Choose cards from " + effectType.getZoneName();
            case SHUFFLECARDSFROMDISCARDINTODRAWDECK, SHUFFLECARDSFROMHANDINTODRAWDECK,
                    SHUFFLECARDSFROMPLAYINTODRAWDECK ->
                    "Choose cards to shuffle into the draw deck";
            case PUTCARDSFROMPLAYONBOTTOMOFDECK ->  "Choose cards in play";
        };
    }

    private static Function<ActionContext, List<PhysicalCard>> getCardSource(String type, Zone fromZone,
                                                                             PlayerSource targetPlayer)
            throws InvalidCardDefinitionException {
        final String sourceMemory = type.startsWith("memory(") ?
                type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")) : null;
        if (fromZone == null) {
            return actionContext -> Filters.filterActive(actionContext.getGame(), sourceMemory == null ? Filters.any :
                    actionContext.getCardFromMemory(sourceMemory)).stream().toList();
        } else {
            return switch (fromZone) {
                case HAND, DISCARD, DRAW_DECK -> actionContext -> Filters.filter(
                        actionContext.getGameState().getZoneCards(targetPlayer.getPlayerId(actionContext), fromZone),
                        sourceMemory == null ?
                                Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
                default -> throw new InvalidCardDefinitionException(
                        "getCardSource function not defined for zone " + fromZone.getHumanReadable());
            };
        }
    }
}