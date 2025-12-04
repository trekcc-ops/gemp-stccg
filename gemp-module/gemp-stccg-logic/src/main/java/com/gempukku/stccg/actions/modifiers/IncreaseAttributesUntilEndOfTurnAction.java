package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.AttributeModifier;

import java.util.List;

public class IncreaseAttributesUntilEndOfTurnAction extends ActionyAction {
    private final ActionCardResolver _cardTarget;
    private final int _amount;
    private final PhysicalCard _performingCard;
    private final List<CardAttribute> _attributes;

    public IncreaseAttributesUntilEndOfTurnAction(List<CardAttribute> attributes, DefaultGame cardGame,
                                                  PhysicalCard performingCard, String playerName,
                                                  ActionCardResolver cardTarget, int amount) {
        super(cardGame, playerName, ActionType.ADD_MODIFIER);
        _cardTarget = cardTarget;
        _amount = amount;
        _performingCard = performingCard;
        _attributes = attributes;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        Modifier modifier = new AttributeModifier(_performingCard, _cardTarget, _amount, _attributes);
        cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(modifier);
        setAsSuccessful();
        return getNextAction();
    }

}