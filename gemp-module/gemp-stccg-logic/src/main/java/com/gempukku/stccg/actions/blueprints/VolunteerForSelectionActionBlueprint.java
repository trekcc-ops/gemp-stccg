package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.RandomSelectionInitiatedResult;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.actions.choose.VolunteerForSelectionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

public class VolunteerForSelectionActionBlueprint extends DefaultActionBlueprint {

    private VolunteerForSelectionActionBlueprint() {
        super(new YouPlayerSource());
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, GameTextContext context) {
        if (context.acceptsAllRequirements(cardGame, _requirements)) {
            if (cardGame.getCurrentActionResult() instanceof RandomSelectionInitiatedResult actionResult &&
                    actionResult.includesCardMatchingFilter(cardGame, Filters.card(context.card()))) {
                SelectRandomCardsAction selectAction = actionResult.getAction();
                return new VolunteerForSelectionAction(cardGame, context.yourName(),
                        context.card(), selectAction);
            }
        }
        return null;
    }
}