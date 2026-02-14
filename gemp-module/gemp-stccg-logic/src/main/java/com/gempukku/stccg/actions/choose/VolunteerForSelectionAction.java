package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class VolunteerForSelectionAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _volunteeringCard;
    private final SelectRandomCardAction _selectAction;

    public VolunteerForSelectionAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard volunteeringCard,
                                       SelectRandomCardAction selectAction) {
        super(cardGame, performingPlayerName, ActionType.VOLUNTEER_FOR_SELECTION);
        _volunteeringCard = volunteeringCard;
        _selectAction = selectAction;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        _selectAction.setCardToRequired(_volunteeringCard);
        setAsSuccessful();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _volunteeringCard;
    }
}