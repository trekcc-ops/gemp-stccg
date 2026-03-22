package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class AddUntilModifierAction extends ActionyAction {
    private final ModifierBlueprint _modifier;
    private final TimeResolver.Time until;

    public AddUntilModifierAction(DefaultGame cardGame, String playerName, ModifierBlueprint modifier,
                                  TimeResolver.Time until, GameTextContext context) {
        super(cardGame, playerName, ActionType.ADD_MODIFIER, context);
        _modifier = modifier;
        this.until = until;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = cardGame.getCurrentPhase();

        Modifier modifier = _modifier.createModifier(cardGame, _actionContext.card(), _actionContext);

        if (until.isEndOfTurn())
            cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(modifier);
        else
            cardGame.getModifiersEnvironment().addUntilEndOfPhaseModifier(modifier, phase);
        setAsSuccessful();
    }

}