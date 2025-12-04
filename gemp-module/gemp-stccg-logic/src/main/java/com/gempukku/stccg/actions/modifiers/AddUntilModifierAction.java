package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

public class AddUntilModifierAction extends ActionyAction {
    private final ModifierBlueprint _modifier;
    private final TimeResolver.Time until;
    private final ActionContext _context;

    public AddUntilModifierAction(DefaultGame cardGame, String playerName, ModifierBlueprint modifier,
                                  TimeResolver.Time until, ActionContext context) {
        super(cardGame, playerName, ActionType.ADD_MODIFIER);
        _modifier = modifier;
        this.until = until;
        _context = context;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = cardGame.getGameState().getCurrentPhase();

        Modifier modifier = _modifier.createModifier(cardGame, _context.getPerformingCard(cardGame), _context);

        if (until.isEndOfTurn())
            cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(modifier);
        else
            cardGame.getModifiersEnvironment().addUntilEndOfPhaseModifier(modifier, phase);
        setAsSuccessful();
        return getNextAction();
    }

}