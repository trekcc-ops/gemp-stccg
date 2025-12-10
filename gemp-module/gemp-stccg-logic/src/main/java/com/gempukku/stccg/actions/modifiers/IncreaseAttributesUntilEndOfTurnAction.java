package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
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
        if (_cardTarget instanceof SelectCardsResolver selectTarget) {
            if (!_cardTarget.isResolved()) {
                Action selectionAction = selectTarget.getSelectionAction();
                if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                    return selectionAction;
                } else {
                    _cardTarget.resolve(cardGame);
                }
            }
        }

        CardFilter affectedCardsFilter = switch(_cardTarget) {
            case SelectCardsResolver selectTarget -> Filters.inCards(_cardTarget.getCards(cardGame));
            case FixedCardsResolver fixedCardsTarget -> Filters.inCards(_cardTarget.getCards(cardGame));
            case FixedCardResolver fixedCardTarget -> Filters.card(fixedCardTarget.getCard());
            case AllCardsMatchingFilterResolver filterTarget -> filterTarget.getFilter();
            default -> throw new InvalidGameLogicException("Unexpected value: " + _cardTarget);
        };

        Modifier modifier = new AttributeModifier(_performingCard, affectedCardsFilter, _amount, _attributes);
        cardGame.getModifiersEnvironment().addUntilEndOfTurnModifier(modifier);
        setAsSuccessful();
        return getNextAction();
    }

}