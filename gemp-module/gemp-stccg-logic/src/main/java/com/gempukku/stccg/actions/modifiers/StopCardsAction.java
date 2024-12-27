package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;

public class StopCardsAction extends ActionyAction {
    private final Collection<ST1EPhysicalCard> _cardsToStop = new LinkedList<>();
    private final SelectCardsAction _selectionAction;
    private boolean _personnelChosen;

    public StopCardsAction(Player performingPlayer, Collection<? extends ST1EPhysicalCard> cardsToStop) {
        super(performingPlayer, "Stop cards", ActionType.STOP_CARDS);
        _personnelChosen = true;
        _cardsToStop.addAll(cardsToStop);
        _selectionAction = new SelectVisibleCardsAction(this, performingPlayer, "", cardsToStop,
                cardsToStop.size());
    }

    public StopCardsAction(Player performingPlayer, SelectCardsAction selectionAction) {
        super(performingPlayer, "Stop cards", ActionType.STOP_CARDS);
        _selectionAction = selectionAction;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return null;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return null;
    }

    @Override
    public String getActionSelectionText(DefaultGame game) {
        if (_personnelChosen && _cardsToStop.size() == 1) {
            return "Stop " + Iterables.getOnlyElement(_cardsToStop).getTitle();
        } else {
            return "Stop personnel";
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_personnelChosen) {
            if (!_selectionAction.wasCarriedOut()) {
                return _selectionAction;
            } else {
                for (PhysicalCard card : _selectionAction.getSelectedCards()) {
                    if (card instanceof ST1EPhysicalCard stCard) {
                        _cardsToStop.add(stCard);
                    } else {
                        throw new InvalidGameLogicException(
                                "Tried to \"stop\" a card from a game with no \"stop\" action");
                    }
                }
                _personnelChosen = true;
            }
        }

        if (!_wasCarriedOut) {
            for (ST1EPhysicalCard card : _cardsToStop) {
                card.stop();
            }
        }

        return getNextAction();
    }
}