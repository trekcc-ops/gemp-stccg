package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.Objects;

public class AddUntilEndOfTurnModifierAction extends ActionyAction implements TopLevelSelectableAction {
    private final Modifier _modifier;
    private final PhysicalCard _performingCard;

    public AddUntilEndOfTurnModifierAction(Player performingPlayer, PhysicalCard performingCard, Modifier modifier) {
        super(performingCard.getGame(), performingPlayer, "Add modifier", ActionType.ADD_MODIFIER);
        _performingCard = Objects.requireNonNull(performingCard);
        _modifier = Objects.requireNonNull(modifier);
    }

    public AddUntilEndOfTurnModifierAction(DefaultGame cardGame, Player performingPlayer, PhysicalCard performingCard,
                                           Modifier modifier) {
        super(cardGame, performingPlayer, "Add modifier", ActionType.ADD_MODIFIER);
        _performingCard = Objects.requireNonNull(performingCard);
        _modifier = Objects.requireNonNull(modifier);
    }



    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null) {
            return cost;
        }
        cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
        return getNextAction();
    }
}