package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("SpellCheckingInspection")
public class PlaceCardsInDrawDeckResult extends AbstractAtTest implements ActionResultTest {

    private MissionCard _mission;
    private Collection<PersonnelCard> attemptingPersonnel;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        PhysicalCard newEssentialists = builder.addSeedCardUnderMission("116_008", "New Essentialists", P2, _mission);
        PersonnelCard data = builder.addCardOnPlanetSurface("101_204", "Data", P1, _mission, PersonnelCard.class);
        PersonnelCard troi = builder.addCardOnPlanetSurface("101_205", "Deanna Troi", P1, _mission, PersonnelCard.class);
        PersonnelCard hobson = builder.addCardOnPlanetSurface("101_202", "Christopher Hobson", P1, _mission, PersonnelCard.class);
        PersonnelCard picard = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, _mission, PersonnelCard.class);
        PersonnelCard sharat = builder.addCardOnPlanetSurface("112_235", "Sharat", P1, _mission, PersonnelCard.class);
        attemptingPersonnel = List.of(data, troi, hobson, picard, sharat);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {

        initializeGame();
        attemptMission(P1, _mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        Collection<PersonnelCard> inDrawDeckPersonnel = new ArrayList<>();

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isInDrawDeck(_game)) {
                inDrawDeckPersonnel.add(personnel);
            }
        }
        assertEquals(1, inDrawDeckPersonnel.size());
        PersonnelCard placedPersonnel = Iterables.getOnlyElement(inDrawDeckPersonnel);

        JsonNode placeInDeckNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("PLACED_CARDS_IN_DRAW_DECK")) {
                placeInDeckNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(placeInDeckNode);
        assertSerializedFields(placeInDeckNode, "targetCardIds", "placement");

        assertEquals(P1, placeInDeckNode.get("performingPlayerId").textValue());
        assertEquals(1, placeInDeckNode.get("targetCardIds").size());
        assertEquals(placedPersonnel.getCardId(), placeInDeckNode.get("targetCardIds").get(0).intValue());
        assertEquals("TOP", placeInDeckNode.get("placement").textValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {

        initializeGame();
        attemptMission(P1, _mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        Collection<PersonnelCard> inDrawDeckPersonnel = new ArrayList<>();

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isInDrawDeck(_game)) {
                inDrawDeckPersonnel.add(personnel);
            }
        }
        assertEquals(1, inDrawDeckPersonnel.size());
        PersonnelCard placedPersonnel = Iterables.getOnlyElement(inDrawDeckPersonnel);

        JsonNode placeInDeckNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("PLACED_CARDS_IN_DRAW_DECK")) {
                placeInDeckNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(placeInDeckNode);
        assertSerializedFields(placeInDeckNode, "targetCardIds", "placement");

        assertEquals(P1, placeInDeckNode.get("performingPlayerId").textValue());
        assertEquals(1, placeInDeckNode.get("targetCardIds").size());
        assertEquals(placedPersonnel.getCardId(), placeInDeckNode.get("targetCardIds").get(0).intValue());
        assertEquals("TOP", placeInDeckNode.get("placement").textValue());
    }



}