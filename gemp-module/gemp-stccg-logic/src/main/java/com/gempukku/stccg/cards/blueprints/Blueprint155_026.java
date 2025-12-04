package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.BottomCardsOfDiscardFilter;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.PhaseRequirement;

import java.util.ArrayList;
import java.util.List;

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

        SubActionBlueprint actionBlueprint;
        boolean _useOldDefinition = false;
        actionBlueprint = getSelectActionNew();

        return new ActivateCardActionBlueprint(
                1,
                List.of(new PhaseRequirement(Phase.EXECUTE_ORDERS)),
                List.of(placeCardsBlueprint),
                List.of(actionBlueprint, discardBlueprint)
        );
    }

    private SubActionBlueprint getSelectActionNew() throws InvalidCardDefinitionException {
        List<String> descriptions = List.of(
                "Modify personnel attributes",
                "Modify ship attributes",
                "Shuffle cards from discard pile into draw deck"
        );

        // choice1
        FilterBlueprint personnelFilter = new FilterBlueprint() {
            @Override
            public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                return Filters.and(Filters.your(actionContext.getPerformingPlayerId()), Filters.inPlay, Filters.unique,
                        CardIcon.TNG_ICON, CardType.PERSONNEL);
            }
        };

        SelectCardTargetBlueprint selectPersonnel =
                new SelectCardTargetBlueprint(personnelFilter, 1, false);
        SubActionBlueprint choice1 = new IncreaseAttributesSubActionBlueprint(
                List.of(CardAttribute.INTEGRITY, CardAttribute.CUNNING, CardAttribute.STRENGTH),
                "endOfThisTurn", selectPersonnel, 2);

        // choice2

        FilterBlueprint shipFilter = new FilterBlueprint() {
            @Override
            public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                return Filters.and(Filters.your(actionContext.getPerformingPlayerId()), Filters.inPlay, CardIcon.TNG_ICON,
                        CardType.SHIP);
            }
        };

        SelectCardTargetBlueprint selectShip = new SelectCardTargetBlueprint(shipFilter, 1, false);
        SubActionBlueprint choice2 =
                new IncreaseAttributesSubActionBlueprint(List.of(CardAttribute.RANGE), "endOfThisTurn", selectShip, 2);

        // choice3

        FilterBlueprint shuffleCardsFilter = new FilterBlueprint() {
            @Override
            public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
                return new BottomCardsOfDiscardFilter(actionContext.getPerformingPlayerId(), 3,
                        Filters.and(CardIcon.TNG_ICON, Filters.or(CardType.PERSONNEL, CardType.SHIP)));
            }
        };

        CardTargetBlueprint shuffleCardsTarget =
                new SelectCardTargetBlueprint(shuffleCardsFilter, 3, false);
        SubActionBlueprint choice3 =
                new ShuffleCardsIntoDrawDeckSubActionBlueprint(shuffleCardsTarget, "you");

        return new SelectAndPerformSubActionBlueprint(descriptions, List.of(choice1, choice2, choice3));
    }


    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame game) {

        List<TopLevelSelectableAction> result = new ArrayList<>();
        TopLevelSelectableAction action = _actionBlueprint.createAction(game, player.getPlayerId(), thisCard);
        if (action != null) {
            result.add(action);
        }
        return result;
    }


}