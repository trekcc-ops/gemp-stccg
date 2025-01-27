package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Collection;

public abstract class MakeDecisionAction extends ActionyAction {
    protected final String _choiceText;

    public MakeDecisionAction(DefaultGame cardGame, Player performingPlayer, String choiceText) {
        super(cardGame, performingPlayer, choiceText, ActionType.MAKE_DECISION);
        _choiceText = choiceText;
    }



    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    protected abstract AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException;

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        AwaitingDecision decision = getDecision(cardGame);
        cardGame.getUserFeedback().sendAwaitingDecision(decision);
        setAsSuccessful();
        return getNextAction();
    }

}