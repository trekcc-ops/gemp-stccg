package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class RevealHandAction extends ActionyAction {

    public RevealHandAction(DefaultGame cardGame, String performingPlayerName) {
        super(cardGame, performingPlayerName, ActionType.REVEAL_HAND_CARD);
    }
    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> cardsInHand = cardGame.getGameState().getZoneCards(_performingPlayerId, Zone.HAND);
        cardsInHand.forEach(PhysicalCard::reveal);
        for (String playerName : cardGame.getAllPlayerIds()) {
            if (!playerName.equals(_performingPlayerId)) {
                AwaitingDecision revealDecision = new ArbitraryCardsSelectionDecision(playerName, "", cardsInHand,
                        0, 0, cardGame) {
                    @Override
                    public void decisionMade(String result) {

                    }
                };
                cardGame.sendAwaitingDecision(revealDecision);
            }
        }
        setAsSuccessful();
    }
}