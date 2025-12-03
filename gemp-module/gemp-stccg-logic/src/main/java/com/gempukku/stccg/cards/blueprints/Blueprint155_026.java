package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.SelectCardsResolver;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.modifiers.AddUntilEndOfTurnModifierAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.actions.usage.UseGameTextAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.BottomCardsOfDiscardFilter;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;
import com.gempukku.stccg.modifiers.attributes.RangeModifier;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class Blueprint155_026 extends CardBlueprint {
    // Get It Done
    Blueprint155_026() {
        super("155_026");
    }

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame game) {
        Phase currentPhase = game.getCurrentPhase();
        List<TopLevelSelectableAction> actions = new LinkedList<>();

        if (currentPhase == Phase.EXECUTE_ORDERS && thisCard.isControlledBy(player)) {
            UseGameTextAction getItDoneAction =
                    new UseGameTextAction(game, thisCard, player, "Discard 2 cards to choose effect");

            getItDoneAction.appendCost(new UseOncePerTurnAction(game, getItDoneAction, thisCard, player.getPlayerId()));
            getItDoneAction.setCardActionPrefix("1");

            CardFilter selectableFilter = Filters.and(Filters.yourHand(player), CardIcon.TNG_ICON);
            SelectVisibleCardsAction selectCardsToPlaceAction = new SelectVisibleCardsAction(game, player,
                    "Select cards to place beneath draw deck", selectableFilter, 2, 2);
            Action costAction = new PlaceCardsOnBottomOfDrawDeckAction(game, player, selectCardsToPlaceAction);
            getItDoneAction.appendCost(costAction);

            TopLevelSelectableAction choice1 = choice1(game, thisCard, player);
            TopLevelSelectableAction choice2 = choice2(game, thisCard, player);
            TopLevelSelectableAction choice3 = choice3(game, thisCard, player);

            List<Action> selectableActions = new ArrayList<>();
            selectableActions.add(choice1);
            selectableActions.add(choice2);
            selectableActions.add(choice3);

            Map<Action, String> actionMessageMap = new HashMap<>();
            actionMessageMap.put(choice1, "Modify personnel attributes");
            actionMessageMap.put(choice2, "Modify ship attributes");
            actionMessageMap.put(choice3, "Shuffle cards from discard pile into draw deck");

            Action chooseAction =
                    new SelectAndInsertAction(game, getItDoneAction, player.getPlayerId(), selectableActions, actionMessageMap);
            getItDoneAction.appendEffect(chooseAction);

            // after any use, discard incident OR discard a [TNG] card from hand
            CardFilter tngCardsInHandFilter = Filters.and(Filters.yourHand(player), CardIcon.TNG_ICON);
            CardFilter discardCardFilter = Filters.or(Filters.card(thisCard), tngCardsInHandFilter);
            SelectVisibleCardAction selectCardToDiscardAction = new SelectVisibleCardAction(game, player,
                    "Select a card to discard", discardCardFilter);
            Action discardAction = new DiscardSingleCardAction(game, thisCard, player, selectCardToDiscardAction);
            getItDoneAction.appendEffect(discardAction);

            actions.add(getItDoneAction);
        }
        return actions;
    }

    private TopLevelSelectableAction choice1(DefaultGame cardGame, PhysicalCard thisCard, Player player) {
        SelectCardsAction targetAction = new SelectCardsFromDialogAction(cardGame, player.getPlayerId(),
                "Select a personnel",
                Filters.and(Filters.your(player), Filters.inPlay, Filters.unique, CardIcon.TNG_ICON,
                        CardType.PERSONNEL));

        ActionCardResolver resolver = new SelectCardsResolver(targetAction);
        Modifier modifier = new AllAttributeModifier(thisCard, resolver, 2);

        TopLevelSelectableAction addModifierAction =
                new AddUntilEndOfTurnModifierAction(cardGame, player, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private TopLevelSelectableAction choice2(DefaultGame cardGame, PhysicalCard thisCard, Player player) {
        SelectVisibleCardAction targetAction = new SelectVisibleCardAction(cardGame, player, "Select a ship",
                Filters.and(Filters.your(player), Filters.inPlay, CardIcon.TNG_ICON,
                        CardType.SHIP));

        ActionCardResolver resolver = new SelectCardsResolver(targetAction);
        Modifier modifier = new RangeModifier(thisCard, resolver, 2);

        TopLevelSelectableAction addModifierAction =
                new AddUntilEndOfTurnModifierAction(cardGame, player, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private TopLevelSelectableAction choice3(DefaultGame cardGame, PhysicalCard thisCard, Player player) {
        String playerName = player.getPlayerId();
        // shuffle the bottom three personnel and/or ships from your discard pile into your draw deck
        CardFilter shuffleCardsFilter = new BottomCardsOfDiscardFilter(playerName, 3, Filters.and(CardIcon.TNG_ICON,
                Filters.or(CardType.PERSONNEL, CardType.SHIP)));
        return new ShuffleCardsIntoDrawDeckAction(cardGame, thisCard, playerName, shuffleCardsFilter);
    }
}