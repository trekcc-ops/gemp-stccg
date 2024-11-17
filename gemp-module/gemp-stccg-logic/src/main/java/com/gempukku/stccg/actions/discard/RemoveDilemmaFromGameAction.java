package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collections;

public class RemoveDilemmaFromGameAction extends ActionyAction {
    private final ST1EPhysicalCard _cardToRemove;
    private final MissionCard _mission;

    public RemoveDilemmaFromGameAction(Player performingPlayer, ST1EPhysicalCard cardToRemove, MissionCard mission) {
        super(performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardToRemove = cardToRemove;
        _mission = mission;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardToRemove;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _cardToRemove;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        _mission.removeSeedCard(_cardToRemove);

        cardGame.getGameState().removeCardsFromZone(_performingPlayerId, Collections.singleton(_cardToRemove));
        cardGame.getGameState().addCardToZone(_cardToRemove, Zone.REMOVED);

        cardGame.sendMessage(_performingPlayerId + " removed " + _cardToRemove.getCardLink() + " from the game");

        return getNextAction();
    }
}