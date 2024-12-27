package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.*;
import com.gempukku.stccg.actions.usage.UseGameTextAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.actions.modifiers.AddUntilEndOfTurnModifierAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;
import com.gempukku.stccg.modifiers.attributes.RangeModifier;

import java.util.LinkedList;
import java.util.List;

public class Blueprint155_026 extends CardBlueprint {
    // Get It Done
    Blueprint155_026() {
        super("155_026");
    }

    @Override
    public List<? extends Action> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard) {
        DefaultGame game = player.getGame();
        Phase currentPhase = game.getCurrentPhase();
        List<Action> actions = new LinkedList<>();

        if (currentPhase == Phase.EXECUTE_ORDERS && thisCard.isControlledBy(player)) {
            ActionyAction getItDoneAction =
                    new UseGameTextAction(thisCard, player, "Discard 2 cards to choose effect");

            getItDoneAction.appendCost(new UseOncePerTurnAction(getItDoneAction, thisCard, player));
            getItDoneAction.setCardActionPrefix("1");

            Filter selectableFilter = Filters.and(Filters.yourHand(player), CardIcon.TNG_ICON);
            SelectVisibleCardsAction selectCardsToPlaceAction = new SelectVisibleCardsAction(thisCard, player,
                    "Select cards to place beneath draw deck", selectableFilter, 2, 2);
            Action costAction = new PlaceCardsOnBottomOfDrawDeckAction(player, selectCardsToPlaceAction, thisCard);
            getItDoneAction.appendCost(costAction);

            Action choice1 = choice1(thisCard, player);
            Action choice2 = choice2(thisCard, player);
            Action choice3 = choice3(thisCard, player);

            Action chooseAction =
                    new SelectAndInsertAction(getItDoneAction, thisCard, player, choice1, choice2, choice3);
            getItDoneAction.appendAction(chooseAction);

            // after any use, discard incident OR discard a [TNG] card from hand
            Filter tngCardsInHandFilter = Filters.and(Filters.yourHand(player), CardIcon.TNG_ICON);
            Filter discardCardFilter = Filters.or(thisCard, tngCardsInHandFilter);
            SelectVisibleCardAction selectCardToDiscardAction = new SelectVisibleCardAction(thisCard, player,
                    "Select a card to discard", discardCardFilter);
            Action discardAction = new DiscardCardAction(thisCard, player, selectCardToDiscardAction);
            getItDoneAction.appendAction(discardAction);

            actions.add(getItDoneAction);
        }
        return actions;
    }

    private Action choice1(PhysicalCard thisCard, Player player) {
        SelectCardsAction targetAction = new SelectCardsFromDialogAction(thisCard, player,
                "Select a personnel",
                Filters.and(Filters.your(player), Filters.inPlay, Filters.unique, CardIcon.TNG_ICON,
                        CardType.PERSONNEL));

        ActionCardResolver resolver = new ActionCardResolver(targetAction);
        Modifier modifier = new AllAttributeModifier(thisCard, resolver, 2);

        ActionyAction addModifierAction = new AddUntilEndOfTurnModifierAction(player, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private Action choice2(PhysicalCard thisCard, Player player) {
        SelectVisibleCardAction targetAction = new SelectVisibleCardAction(thisCard, player, "Select a ship",
                Filters.and(Filters.your(player), Filters.inPlay, CardIcon.TNG_ICON,
                        CardType.SHIP));

        ActionCardResolver resolver = new ActionCardResolver(targetAction);
        Modifier modifier = new RangeModifier(thisCard, resolver, 2);

        ActionyAction addModifierAction = new AddUntilEndOfTurnModifierAction(player, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private Action choice3(PhysicalCard thisCard, Player player) {
        // shuffle the bottom three personnel and/or ships from your discard pile into your draw deck
        Filter shuffleCardsFilter = Filters.bottomCardsOfDiscard(player, 3, CardIcon.TNG_ICON,
                Filters.or(CardType.PERSONNEL, CardType.SHIP));
        return new ShuffleCardsIntoDrawDeckAction(thisCard, player, shuffleCardsFilter);
    }
}