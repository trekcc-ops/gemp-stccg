package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.actions.choose.SelectAndInsertAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.modifiers.AddUntilEndOfTurnModifierAction;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.BottomCardsOfDiscardFilter;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AllAttributeModifier;
import com.gempukku.stccg.modifiers.attributes.RangeModifier;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.PhaseRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Blueprint155_026 extends CardBlueprint {
    // Get It Done

    private final ActionBlueprint _actionBlueprint;

    Blueprint155_026() throws InvalidCardDefinitionException {
        super("155_026");
        _actionBlueprint = getActionBlueprint();
    }

    public ActivateCardActionBlueprint getActionBlueprint() throws InvalidCardDefinitionException {

        FilterBlueprint inYourHandFilter = new FilterBlueprint() {

            @Override
            public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                return Filters.and(Filters.yourHand(actionContext.getPerformingPlayerId()), CardIcon.TNG_ICON);
            }
        };

        SelectCardTargetBlueprint cardTarget = new SelectCardTargetBlueprint(
                inYourHandFilter,
                2,
                false
        );

        PlaceCardsOnBottomOfDrawDeckSubactionBlueprint placeCardsBlueprint =
                new PlaceCardsOnBottomOfDrawDeckSubactionBlueprint(
                        cardTarget,
                        "you"
                );

/*        List<String> descriptions = List.of(
                "Modify personnel attributes",
                "Modify ship attributes",
                "Shuffle cards from discard pile into draw deck"
        ); */

/*        SelectAndPerformSubActionBlueprint actionBlueprint = new SelectAndPerformSubActionBlueprint(
                descriptions,
                List<SubActionBlueprint> subActions
        ); */

        SubActionBlueprint actionBlueprint = new SubActionBlueprint() {
            @Override
            public List<Action> createActions(DefaultGame cardGame, CardPerformedAction action,
                                              ActionContext actionContext) throws InvalidGameLogicException {
                PhysicalCard thisCard = actionContext.getPerformingCard(cardGame);
                String playerName = actionContext.getPerformingPlayerId();

                Action choice1 = choice1(cardGame, thisCard, playerName);
                Action choice2 = choice2(cardGame, thisCard, playerName);
                Action choice3 = choice3(cardGame, thisCard, playerName);
                List<Action> selectableActions = List.of(choice1, choice2, choice3);

                Map<Action, String> actionMessageMap = new HashMap<>();
                actionMessageMap.put(choice1, "Modify personnel attributes");
                actionMessageMap.put(choice2, "Modify ship attributes");
                actionMessageMap.put(choice3, "Shuffle cards from discard pile into draw deck");

                Action chooseAction =
                        new SelectAndInsertAction(cardGame, action, playerName, selectableActions, actionMessageMap);
                return List.of(chooseAction);
            }
        };


        FilterBlueprint discardFilter = new FilterBlueprint() {
            @Override
            public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                try {
                    return Filters.or(Filters.card(actionContext.getPerformingCard(cardGame)),
                            Filters.and(Filters.yourHand(actionContext.getPerformingPlayerId()), CardIcon.TNG_ICON)
                    );
                } catch(InvalidGameLogicException exp) {
                    cardGame.sendErrorMessage(exp);
                    return null;
                }
            }
        };

        SelectCardTargetBlueprint discardTarget =
                new SelectCardTargetBlueprint(discardFilter, 1, false);
        DiscardSubActionBlueprint discardBlueprint = new DiscardSubActionBlueprint(discardTarget);

        return new ActivateCardActionBlueprint(
                1,
                List.of(new PhaseRequirement(Phase.EXECUTE_ORDERS)),
                List.of(placeCardsBlueprint),
                List.of(actionBlueprint, discardBlueprint)
        );
    }

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame game) {

        boolean _oldDef = false;
        List<TopLevelSelectableAction> result = new ArrayList<>();
        TopLevelSelectableAction action = _actionBlueprint.createAction(game, player.getPlayerId(), thisCard);
        if (action != null) {
            result.add(action);
        }
        return result;
    }


    private TopLevelSelectableAction choice1(DefaultGame cardGame, PhysicalCard thisCard, String playerName) {
        SelectCardsAction targetAction = new SelectCardsFromDialogAction(cardGame, playerName,
                "Select a personnel",
                Filters.and(Filters.your(playerName), Filters.inPlay, Filters.unique, CardIcon.TNG_ICON,
                        CardType.PERSONNEL));

        ActionCardResolver resolver = new SelectCardsResolver(targetAction);
        Modifier modifier = new AllAttributeModifier(thisCard, resolver, 2);

        TopLevelSelectableAction addModifierAction =
                new AddUntilEndOfTurnModifierAction(cardGame, playerName, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private TopLevelSelectableAction choice2(DefaultGame cardGame, PhysicalCard thisCard, String playerName) {
        SelectVisibleCardAction targetAction = new SelectVisibleCardAction(cardGame, playerName, "Select a ship",
                Filters.and(Filters.your(playerName), Filters.inPlay, CardIcon.TNG_ICON,
                        CardType.SHIP));

        ActionCardResolver resolver = new SelectCardsResolver(targetAction);
        Modifier modifier = new RangeModifier(thisCard, resolver, 2);

        TopLevelSelectableAction addModifierAction =
                new AddUntilEndOfTurnModifierAction(cardGame, playerName, thisCard, modifier);
        addModifierAction.appendCost(targetAction);
        return addModifierAction;
    }

    private TopLevelSelectableAction choice3(DefaultGame cardGame, PhysicalCard thisCard, String playerName) {
        // shuffle the bottom three personnel and/or ships from your discard pile into your draw deck
        CardFilter shuffleCardsFilter = new BottomCardsOfDiscardFilter(playerName, 3, Filters.and(CardIcon.TNG_ICON,
                Filters.or(CardType.PERSONNEL, CardType.SHIP)));
        return new ShuffleCardsIntoDrawDeckAction(cardGame, thisCard, playerName, shuffleCardsFilter);
    }
}