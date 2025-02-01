package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.game.*;
import com.google.common.collect.Iterables;

public class KillSinglePersonnelAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;

    public KillSinglePersonnelAction(Player performingPlayer, PhysicalCard performingCard,
                                     SelectCardsAction selectVictimAction) {
        super(performingCard.getGame(), performingPlayer, "Kill", ActionType.KILL);
        _performingCard = performingCard;
        _cardTarget = new SelectCardsResolver(selectVictimAction);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return getPerformingCard().getCardId();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public String getActionSelectionText(DefaultGame cardGame) throws InvalidGameLogicException {
        StringBuilder sb = new StringBuilder();
        sb.append("Kill ");
        if (_cardTarget.isResolved() && _cardTarget.getCards(cardGame).size() == 1) {
            PhysicalCard victim = Iterables.getOnlyElement(_cardTarget.getCards(cardGame));
            sb.append(victim.getTitle());
        } else {
            sb.append("a personnel");
        }
        return sb.toString();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        if (!_wasCarriedOut) {
            if (_cardTarget.getCards(cardGame).size() != 1) {
                throw new InvalidGameLogicException("Too many cards selected for KillSinglePersonnelAction");
            } else {
                PhysicalCard victim = Iterables.getOnlyElement(_cardTarget.getCards(cardGame));
                StringBuilder message = new StringBuilder();
                message.append(_performingPlayerId).append(" killed ").append(victim.getCardLink());
                if (_performingCard != null)
                    message.append(" using ").append(_performingCard.getCardLink());
                cardGame.sendMessage(message.toString());

                if (victim instanceof PhysicalReportableCard1E reportable && reportable.getAwayTeam() != null)
                    reportable.leaveAwayTeam((ST1EGame) cardGame);
                _wasCarriedOut = true;
                return new DiscardCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), victim);
            }
        }
        setAsSuccessful();
        return getNextAction();
    }
}