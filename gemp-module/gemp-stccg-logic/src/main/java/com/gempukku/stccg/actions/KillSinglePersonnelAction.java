package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.SelectCardInPlayAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

public class KillSinglePersonnelAction extends ActionyAction {

    private final PhysicalCard _performingCard;
    private final SelectCardInPlayAction _selectVictimAction;
    private boolean _victimSelected;
    private PhysicalCard _victim;

    public KillSinglePersonnelAction(Player performingPlayer, PhysicalCard performingCard,
                                     SelectCardInPlayAction selectVictimAction) {
        super(performingPlayer, "Kill", ActionType.KILL);
        _performingCard = performingCard;
        _selectVictimAction = selectVictimAction;
    }
    @Override
    public PhysicalCard getActionSource() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_victimSelected) {
            if (!_selectVictimAction.wasCarriedOut())
                return _selectVictimAction;
            else {
                _victimSelected = true;
                _victim = _selectVictimAction.getSelectedCard();
            }
        }

        if (!_wasCarriedOut) {
            StringBuilder message = new StringBuilder();
            message.append(_performingPlayerId).append(" killed ").append(_victim.getCardLink());
            if (_performingCard != null)
                message.append(" using ").append(_performingCard.getCardLink());
            cardGame.sendMessage(message.toString());

            if (_victim instanceof PhysicalReportableCard1E reportable && reportable.getAwayTeam() != null)
                reportable.leaveAwayTeam();
            _wasCarriedOut = true;
            return new DiscardCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), _victim);
        }

        return getNextAction();
    }

}