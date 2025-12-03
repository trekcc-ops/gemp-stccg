package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilModifierAction extends ActionyAction {
    private final Modifier _modifier;
    private final TimeResolver.Time until;

    public AddUntilModifierAction(DefaultGame cardGame, String playerName, Modifier modifier,
                                  TimeResolver.Time until) {
        super(cardGame, playerName, ActionType.ADD_MODIFIER);
        _modifier = modifier;
        this.until = until;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = cardGame.getGameState().getCurrentPhase();

        if (until.isEndOfTurn())
            cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
        else
            cardGame.getModifiersEnvironment().addUntilEndOfPhaseModifier(_modifier, phase);
        setAsSuccessful();
        return getNextAction();
    }

}