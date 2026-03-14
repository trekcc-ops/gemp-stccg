package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnterPlayResultTest extends AbstractAtTest implements ActionResultTest {

    private FacilityCard outpost;
    private PhysicalCard ams;
    private PhysicalCard tarses1;
    private PhysicalCard wallace1;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        ams = builder.addSeedDeckCard("109_063", "Assign Mission Specialists", P1);
        tarses1 = builder.addDrawDeckCard("101_236", "Simon Tarses", P1);
        wallace1 = builder.addDrawDeckCard("101_203", "Darian Wallace", P1);
        builder.addDrawDeckCard("101_236", "Simon Tarses", P2);
        builder.addDrawDeckCard("101_203", "Darian Wallace", P2);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();

        seedCard(P1, ams);
        assertTrue(ams.isInPlay());
        playerSaysYes(P1); // use optional download action

        List<PersonnelCard> specialists = List.of((PersonnelCard) tarses1, (PersonnelCard) wallace1);
        selectCards(P1, specialists);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode seedNode = null;
        JsonNode playTarsesNode = null;
        JsonNode playWallaceNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("SEEDED_INTO_PLAY") &&
                    resultsNode.get(i).get("seededCardId").intValue() == ams.getCardId()) {
                seedNode = resultsNode.get(i);
                playTarsesNode = resultsNode.get(i+1);
                playWallaceNode = resultsNode.get(i+2);
                assertEquals("PLAYED_CARD", playTarsesNode.get("type").textValue());
                assertEquals("PLAYED_CARD", playWallaceNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(seedNode);
        assertNotNull(playTarsesNode);
        assertNotNull(playWallaceNode);

        assertSerializedFields(seedNode, "seededCardId", "toCore");
        assertSerializedFields(playTarsesNode, "playedCardId", "destinationCardId", "toCore", 
                "isDownload", "isReport", "performingCardId");
        assertSerializedFields(playWallaceNode, "playedCardId", "destinationCardId", "toCore",
                "isDownload", "isReport", "performingCardId");
        
        assertEquals(ams.getCardId(), seedNode.get("seededCardId").intValue());
        assertTrue(seedNode.get("toCore").booleanValue());
        
        assertEquals(tarses1.getCardId(), playTarsesNode.get("playedCardId").intValue());
        assertEquals(outpost.getCardId(), playTarsesNode.get("destinationCardId").intValue());
        assertFalse(playTarsesNode.get("toCore").booleanValue());
        assertTrue(playTarsesNode.get("isDownload").booleanValue());
        assertTrue(playTarsesNode.get("isReport").booleanValue());
        assertEquals(ams.getCardId(), playTarsesNode.get("performingCardId").intValue());

        assertEquals(wallace1.getCardId(), playWallaceNode.get("playedCardId").intValue());
        assertEquals(outpost.getCardId(), playWallaceNode.get("destinationCardId").intValue());
        assertFalse(playWallaceNode.get("toCore").booleanValue());
        assertTrue(playWallaceNode.get("isDownload").booleanValue());
        assertTrue(playWallaceNode.get("isReport").booleanValue());
        assertEquals(ams.getCardId(), playWallaceNode.get("performingCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();

        seedCard(P1, ams);
        assertTrue(ams.isInPlay());
        playerSaysYes(P1); // use optional download action

        List<PersonnelCard> specialists = List.of((PersonnelCard) tarses1, (PersonnelCard) wallace1);
        selectCards(P1, specialists);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode seedNode = null;
        JsonNode playTarsesNode = null;
        JsonNode playWallaceNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("SEEDED_INTO_PLAY") &&
                    resultsNode.get(i).get("seededCardId").intValue() == ams.getCardId()) {
                seedNode = resultsNode.get(i);
                playTarsesNode = resultsNode.get(i+1);
                playWallaceNode = resultsNode.get(i+2);
                assertEquals("PLAYED_CARD", playTarsesNode.get("type").textValue());
                assertEquals("PLAYED_CARD", playWallaceNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(seedNode);
        assertNotNull(playTarsesNode);
        assertNotNull(playWallaceNode);

        assertSerializedFields(seedNode, "seededCardId", "toCore");
        assertSerializedFields(playTarsesNode, "playedCardId", "destinationCardId", "toCore",
                "isDownload", "isReport", "performingCardId");
        assertSerializedFields(playWallaceNode, "playedCardId", "destinationCardId", "toCore",
                "isDownload", "isReport", "performingCardId");

        assertEquals(ams.getCardId(), seedNode.get("seededCardId").intValue());
        assertTrue(seedNode.get("toCore").booleanValue());

        assertEquals(tarses1.getCardId(), playTarsesNode.get("playedCardId").intValue());
        assertEquals(outpost.getCardId(), playTarsesNode.get("destinationCardId").intValue());
        assertFalse(playTarsesNode.get("toCore").booleanValue());
        assertTrue(playTarsesNode.get("isDownload").booleanValue());
        assertTrue(playTarsesNode.get("isReport").booleanValue());
        assertEquals(ams.getCardId(), playTarsesNode.get("performingCardId").intValue());

        assertEquals(wallace1.getCardId(), playWallaceNode.get("playedCardId").intValue());
        assertEquals(outpost.getCardId(), playWallaceNode.get("destinationCardId").intValue());
        assertFalse(playWallaceNode.get("toCore").booleanValue());
        assertTrue(playWallaceNode.get("isDownload").booleanValue());
        assertTrue(playWallaceNode.get("isReport").booleanValue());
        assertEquals(ams.getCardId(), playWallaceNode.get("performingCardId").intValue());
    }

}