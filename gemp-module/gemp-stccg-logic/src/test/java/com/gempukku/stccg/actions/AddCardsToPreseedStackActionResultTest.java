package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyAnonymousCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddCardsToPreseedStackActionResultTest extends AbstractAtTest {

    private MissionCard mission;
    private List<PhysicalCard> cardsToSeed;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P2);
        cardsToSeed = new ArrayList<>();
        cardsToSeed.add(builder.addSeedDeckCard("101_015", "Armus: Skin of Evil", P1));
        cardsToSeed.add(builder.addSeedDeckCard("101_012", "Anaphasic Organism", P1));
        builder.addSeedDeckCard("101_014", "Archer", P1);
        builder.setPhase(Phase.SEED_DILEMMA);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        Action seedAction = seedDilemma(cardsToSeed.getFirst(), mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode preSeedNode = resultsNode.get(1);
        assertEquals(5, preSeedNode.size());

        assertTrue(preSeedNode.has("timestamp"));
        assertTrue(preSeedNode.has("targetCardIds"));
        assertTrue(preSeedNode.has("resultId"));
        assertTrue(preSeedNode.has("type"));
        assertTrue(preSeedNode.has("performingPlayerId"));

        assertEquals("ADD_CARDS_TO_PRESEED_STACK", preSeedNode.get("type").textValue());
        assertEquals(P1, preSeedNode.get("performingPlayerId").textValue());
        assertEquals(1, preSeedNode.get("targetCardIds").size());
        assertEquals(cardsToSeed.getFirst().getCardId(), preSeedNode.get("targetCardIds").get(0).asInt());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        Action seedAction = seedDilemma(cardsToSeed.getFirst(), mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode preSeedNode = resultsNode.get(1);
        assertEquals(5, preSeedNode.size());

        assertTrue(preSeedNode.has("timestamp"));
        assertTrue(preSeedNode.has("targetCardIds"));
        assertTrue(preSeedNode.has("resultId"));
        assertTrue(preSeedNode.has("type"));
        assertTrue(preSeedNode.has("performingPlayerId"));

        assertEquals("ADD_CARDS_TO_PRESEED_STACK", preSeedNode.get("type").textValue());
        assertEquals(P1, preSeedNode.get("performingPlayerId").textValue());
        assertEquals(1, preSeedNode.get("targetCardIds").size());
        assertEquals(new ProxyAnonymousCard(P1).getCardId(), preSeedNode.get("targetCardIds").get(0).asInt());
    }

}