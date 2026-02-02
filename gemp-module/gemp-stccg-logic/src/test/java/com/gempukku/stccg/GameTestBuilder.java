package com.gempukku.stccg;

import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.st1e.ST1EPlayPhaseSegmentProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTestBuilder {

    List<MissionCard> _missions = new ArrayList<>();
    private static final String DEFAULT_MISSION = "101_174";
    private static final String DEFAULT_MISSION_TITLE = "Khitomer Research";
    private final ST1EGame _game;

    public GameTestBuilder(CardBlueprintLibrary cardBlueprintLibrary, FormatLibrary formatLibrary,
                           List<String> playerNames) throws InvalidGameOperationException {
        Map<String, CardDeck> decks = new HashMap<>();
        GameFormat format = formatLibrary.get("debug1e");
        CardDeck testDeck = new CardDeck("Test", format);
        for (int i = 0; i < 30; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "991_001"); // dummy 1E dilemma; 101_104 is Fed Outpost
        }

        for (String player : playerNames) {
            decks.put(player, testDeck);
        }
        _game = new ST1EGame(format, decks, cardBlueprintLibrary, GameTimer.GLACIAL_TIMER);

        // Initialize player order
        _game.getGameState().getCurrentProcess().process(_game);
    }

    public void setPhase(Phase phase) {
        _game.setCurrentPhase(phase);
        if (phase == Phase.EXECUTE_ORDERS) {
            _game.getGameState().setCurrentProcess(new ST1EPlayPhaseSegmentProcess());
        }
    }

    public ST1EGame getGame() {
        return _game;
    }

    private <T extends PhysicalCard> T addCardToGame(String blueprintId, String cardTitle, String ownerName,
                                                     Class<T> clazz) throws CardNotFoundException {
        PhysicalCard cardToAdd = _game.addCardToGame(blueprintId, ownerName);
        if (cardToAdd.getTitle().equals(cardTitle) && clazz.isAssignableFrom(cardToAdd.getClass())) {
            return (T) cardToAdd;
        } else {
            throw new CardNotFoundException("Card added to game either does not match expected title or expected class");
        }
    }

    public MissionCard addMission(String blueprintId, String cardTitle, String ownerName) throws CardNotFoundException {
        MissionCard mission = addCardToGame(blueprintId, cardTitle, ownerName, MissionCard.class);
        _missions.add(mission);
        SeedMissionCardAction seedAction = new SeedMissionCardAction(_game, mission, _missions.indexOf(mission));
        seedAction.processEffect(_game);
        return mission;
    }

    public FacilityCard addFacility(String facilityBlueprintId, String ownerName) throws CardNotFoundException {
        if (_missions.isEmpty()) {
            addMission(DEFAULT_MISSION, DEFAULT_MISSION_TITLE, ownerName);
        }

        PhysicalCard facilityCard = _game.addCardToGame(facilityBlueprintId, ownerName);
        if (facilityCard instanceof FacilityCard facility) {
            SeedOutpostAction seedAction = new SeedOutpostAction(_game, facility, _missions.getFirst());
            seedAction.setAffiliation(facility.getCurrentAffiliations().getFirst());
            seedAction.processEffect(_game);
            assertTrue(facilityCard.isInPlay());
            return facility;
        } else {
            throw new CardNotFoundException("Card incorrect type: " + facilityCard.getClass().getSimpleName());
        }
    }

    public <T extends PhysicalCard> T addCardInHand(String blueprintId, String cardTitle, String ownerName,
                                                    Class<T> clazz) throws CardNotFoundException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.HAND, null);
        assertTrue(cardToAdd.isInHand(_game));
        assertFalse(cardToAdd.isInPlay());
        return cardToAdd;
    }

}