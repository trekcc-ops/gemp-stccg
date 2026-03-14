package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DrawCardsResultTest extends AbstractAtTest {

    @Test
    public void serializeForYouTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.setPhase(Phase.CARD_PLAY);
        _game = builder.startGame();

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode yourDrawNode = null;
        JsonNode opponentDrawNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DREW_CARDS") &&
                    resultsNode.get(i).get("performingPlayerId").textValue().equals(P1)
            ) {
                yourDrawNode = resultsNode.get(i);
                break;
            }
        }

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DREW_CARDS") &&
                    resultsNode.get(i).get("performingPlayerId").textValue().equals(P2)
            ) {
                opponentDrawNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(yourDrawNode);
        assertNotNull(opponentDrawNode);
        assertEquals(opponentDrawNode.get("resultId").intValue(), yourDrawNode.get("resultId").intValue() + 1);
        assertTrue(yourDrawNode.get("isStartingHand").booleanValue());
        assertTrue(opponentDrawNode.get("isStartingHand").booleanValue());

        assertEquals(6, yourDrawNode.size());
        assertEquals(6, opponentDrawNode.size());

        assertTrue(jsonListIsCardIds(yourDrawNode.get("drawnCardIds"), _game.getPlayer(P1).getCardsInHand()));
        assertEquals("[-999,-999,-999,-999,-999,-999,-999]", opponentDrawNode.get("drawnCardIds").toString());
    }

}