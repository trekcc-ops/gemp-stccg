package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.RandomSelectionInitiatedResult;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.actions.choose.VolunteerForSelectionAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

public class VolunteerForSelectionActionBlueprint extends DefaultActionBlueprint {

    private VolunteerForSelectionActionBlueprint() {
        super(new YouPlayerSource());
    }

    @Override
    public TopLevelSelectableAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        ActionContext context = new ActionContext(thisCard, performingPlayerName);
        if (isValid(cardGame, context)) {
            if (cardGame.getCurrentActionResult() instanceof RandomSelectionInitiatedResult actionResult &&
                    actionResult.includesCardMatchingFilter(cardGame, Filters.card(thisCard))) {
                SelectRandomCardsAction selectAction = actionResult.getAction();
                return new VolunteerForSelectionAction(cardGame, performingPlayerName,
                        thisCard, selectAction);
            }
        }
        return null;
    }
}