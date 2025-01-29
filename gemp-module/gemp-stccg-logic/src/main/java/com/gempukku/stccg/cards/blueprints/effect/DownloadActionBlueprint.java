package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class DownloadActionBlueprint extends MultiSubActionBlueprint {

    private final String _memoryId;

    DownloadActionBlueprint(@JsonProperty(value = "filter", required = true)
                            String filter,
                            @JsonProperty(value = "memoryId")
                            String memoryId) throws InvalidCardDefinitionException {

        _memoryId = (memoryId == null) ? "_temp" : memoryId;
        // Get blueprint parameters
        final PlayerSource selectingPlayer = ActionContext::getPerformingPlayerId;
        final PlayerSource targetPlayerSource = ActionContext::getPerformingPlayerId;
        final String defaultText = "Choose card to download";

        FilterableSource cardFilter = (filter.startsWith("all(") || filter.startsWith("choose(")) ?
                new FilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")"))) :
                null;

        ValueSource count = new ConstantEvaluator(1);

        playabilityCheckedForEffect = true;
        setPlayabilityCheckedForEffect(true);

        FilterableSource choiceFilter = (actionContext) -> Filters.playable;

        Function<ActionContext, List<PhysicalCard>> cardSource = getCardSource(filter, targetPlayerSource);

        addEffectBlueprint(CardResolver.resolveCardsInZone(filter, choiceFilter, count, _memoryId,
                selectingPlayer, targetPlayerSource, defaultText, cardFilter, Zone.DRAW_DECK,
                cardSource));

        addEffectBlueprint(
                new DelayedEffectBlueprint() {
                    @Override
                    protected List<Action> createActions(CardPerformedAction parentAction, ActionContext context) {
                        final Collection<PhysicalCard> cardsFromMemory = context.getCardsFromMemory(_memoryId);
                        final List<Collection<PhysicalCard>> effectCardLists = new LinkedList<>();

                        effectCardLists.add(cardsFromMemory);

                        List<Action> subActions = new LinkedList<>();
                        for (Collection<PhysicalCard> cards : effectCardLists) {
                            Action subAction = Iterables.getOnlyElement(cards).getPlayCardAction(true);
                            subActions.add(subAction);
                        }
                        return subActions;
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        return !actionContext.getGame().getModifiersQuerying().hasFlagActive(
                                ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }
                });
    }

    private static Function<ActionContext, List<PhysicalCard>> getCardSource(String type, PlayerSource targetPlayer) {
        final String sourceMemory = type.startsWith("memory(") ?
                type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")) : null;
        return actionContext -> {
            try {
                String playerId = targetPlayer.getPlayerId(actionContext);
                Player target = actionContext.getGame().getPlayer(playerId);
                return Filters.filter(
                        actionContext.getGameState().getZoneCards(target, Zone.DRAW_DECK),
                        sourceMemory == null ? Filters.any :
                                Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
            } catch (PlayerNotFoundException exp) {
                DefaultGame cardGame = actionContext.getGame();
                cardGame.sendErrorMessage(exp);
                cardGame.cancelGame();
                return null;
            }
        };
    }
}