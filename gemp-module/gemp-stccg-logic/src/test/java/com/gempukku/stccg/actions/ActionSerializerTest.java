package com.gempukku.stccg.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

public class ActionSerializerTest extends AbstractAtTest {

       @Test
    public void killAttemptSerializerTest() throws CardNotFoundException, DecisionResultInvalidException,
            JsonProcessingException, PlayerNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        KillSinglePersonnelAction action = new KillSinglePersonnelAction(_game, P1, _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game, _game.getPlayer(P1), "Select a card", Filters.any));
        KillSinglePersonnelAction action2 = new KillSinglePersonnelAction(_game, P1, _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game, _game.getPlayer(P1), "Select a card", Filters.any));
        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);
        action.appendCost(action2);
        String jsonString = _game.getGameState().serializeForPlayer(P1);
        System.out.println(jsonString);
    }

}