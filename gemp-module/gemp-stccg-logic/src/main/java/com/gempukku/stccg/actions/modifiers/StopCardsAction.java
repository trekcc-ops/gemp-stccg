package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
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
    private final ActionCardResolver _cardTarget;
    private enum Progress { cardsChosen }

    public StopCardsAction(Player performingPlayer, Collection<? extends ST1EPhysicalCard> cardsToStop) {
        super(performingPlayer, "Stop cards", ActionType.STOP_CARDS, Progress.values());
        _cardTarget = new ActionCardResolver(cardsToStop);
        setProgress(Progress.cardsChosen);
    }

    public StopCardsAction(Player performingPlayer, SelectCardsAction selectionAction) {
        super(performingPlayer, "Stop cards", ActionType.STOP_CARDS, Progress.values());
        _cardTarget = new ActionCardResolver(selectionAction);
    }

    @Override
    public String getActionSelectionText(DefaultGame game) throws InvalidGameLogicException {
        if (_cardTarget.isResolved() && _cardTarget.getCards(game).size() == 1) {
            return "Stop" + Iterables.getOnlyElement(_cardTarget.getCards(game)).getTitle();
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
        if (!getProgress(Progress.cardsChosen)) {
            if (!_cardTarget.isResolved()) {
                Action selectionAction = _cardTarget.getSelectionAction();
                if (selectionAction != null) {
                    if (selectionAction.wasCarriedOut()) {
                        _cardTarget.resolve(cardGame);
                    } else {
                        return selectionAction;
                    }
                } else {
                    _cardTarget.resolve(cardGame);
                }
            } else {
                setProgress(Progress.cardsChosen);
            }
        }

        if (!_wasCarriedOut) {
            Collection<ST1EPhysicalCard> cardsToStop = new LinkedList<>();
            for (PhysicalCard card : _cardTarget.getCards(cardGame)) {
                if (card instanceof ST1EPhysicalCard stCard) {
                    cardsToStop.add(stCard);
                } else {
                    throw new InvalidGameLogicException(
                            "Tried to \"stop\" a card from a game with no \"stop\" action");
                }
            }
            for (ST1EPhysicalCard card : cardsToStop) {
                card.stop();
            }
            _wasCarriedOut = true;
        }

        return getNextAction();
    }
}