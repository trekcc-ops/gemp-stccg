package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.st1e.*;

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
    private final Map<String, CardDeck> _decks = new HashMap<>();
    private Phase _startingPhase;
    private final List<String> _players;

    public GameTestBuilder(CardBlueprintLibrary cardBlueprintLibrary, FormatLibrary formatLibrary,
                           List<String> playerNames) throws InvalidGameOperationException {
        GameFormat format = formatLibrary.get("debug1e");
        CardDeck testDeck = new CardDeck("Test", format);
        for (int i = 0; i < 30; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "991_001"); // dummy 1E dilemma
        }

        for (String player : playerNames) {
            _decks.put(player, testDeck);
        }
        _game = new ST1EGame(format, _decks, cardBlueprintLibrary, GameTimer.GLACIAL_TIMER);
        _startingPhase = Phase.SEED_DOORWAY;
        _players = playerNames;
    }

    public void startGame() throws InvalidGameOperationException {
        // Initialize player order
        _game.getGameState().getCurrentProcess().process(_game);

        if (!_startingPhase.isSeedPhase()) {
            // draw starting hand
            GameProcess facilityProcess = new ST1EFacilitySeedPhaseProcess(_players.size());
            facilityProcess.getNextProcess(_game);
            // start turn
            _game.getGameState().signalStartOfTurn(_game, _players.getFirst());
        }

        GameProcess currentProcess = switch(_startingPhase) {
            case END_OF_TURN -> new ST1EEndOfTurnProcess();
            case SEED_DOORWAY -> new DoorwaySeedPhaseProcess(_players);
            case SEED_MISSION -> new ST1EMissionSeedPhaseProcess(0);
            case SEED_DILEMMA -> new DilemmaSeedPhaseOpponentsMissionsProcess(_players);
            case SEED_FACILITY -> new ST1EFacilitySeedPhaseProcess(0);
            case CARD_PLAY, EXECUTE_ORDERS -> new ST1EPlayPhaseSegmentProcess();
            case BETWEEN_TURNS, TRIBBLES_TURN, START_OF_TURN -> throw new InvalidGameOperationException(
                    "Unequipped to create test game starting in phase '" + _startingPhase + "'");
        };

        _game.getGameState().setCurrentProcess(currentProcess);
        _game.setCurrentPhase(_startingPhase);
        _game.startGame();
    }

    public void setPhase(Phase phase) {
        _startingPhase = phase;
    }

    public ST1EGame getGame() {
        return _game;
    }

    private PhysicalCard addCardToGame(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = _game.addCardToGame(blueprintId, ownerName);
        if (cardToAdd.getTitle().equals(cardTitle)) {
            return cardToAdd;
        } else {
            throw new CardNotFoundException("Card added to game does not match expected title");
        }
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

    private void executeAction(Action action) throws InvalidGameOperationException {
        action.setAsInitiated();
        action.executeNextSubAction(_game.getActionsEnvironment(), _game);
    }

    public MissionCard addMission(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        MissionCard mission = addCardToGame(blueprintId, cardTitle, ownerName, MissionCard.class);
        _missions.add(mission);
        SeedMissionCardAction seedAction = new SeedMissionCardAction(_game, mission, _missions.indexOf(mission));
        executeAction(seedAction);
        return mission;
    }

    public ShipCard addShipInSpace(String shipBlueprintId, String cardTitle, String ownerName, MissionCard mission)
            throws CardNotFoundException, InvalidGameOperationException {
        ShipCard shipCard = addCardToGame(shipBlueprintId, cardTitle, ownerName, ShipCard.class);
        ReportCardAction playAction = new ReportCardAction(_game, shipCard, true, mission);
        playAction.setAffiliation(shipCard.getCurrentAffiliations().getFirst());
        executeAction(playAction);
        assertTrue(shipCard.isInPlay());
        assertTrue(shipCard.isAtSameLocationAsCard(mission));
        return shipCard;
    }

    public FacilityCard addFacility(String facilityBlueprintId, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        if (_missions.isEmpty()) {
            addMission(DEFAULT_MISSION, DEFAULT_MISSION_TITLE, ownerName);
        }
        return addFacility(facilityBlueprintId, ownerName, _missions.getFirst());
    }

    public FacilityCard addFacility(String facilityBlueprintId, String ownerName, MissionCard mission)
            throws CardNotFoundException, InvalidGameOperationException {
        PhysicalCard facilityCard = _game.addCardToGame(facilityBlueprintId, ownerName);
        if (facilityCard instanceof FacilityCard facility) {
            SeedOutpostAction seedAction = new SeedOutpostAction(_game, facility, mission);
            seedAction.setAffiliation(facility.getCurrentAffiliations().getFirst());
            executeAction(seedAction);
            assertTrue(facilityCard.isInPlay());
            return facility;
        } else {
            throw new CardNotFoundException("Card incorrect type: " + facilityCard.getClass().getSimpleName());
        }
    }


    public PhysicalCard addCardInHand(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.HAND, null);
        assertTrue(cardToAdd.isInHand(_game));
        assertFalse(cardToAdd.isInPlay());
        return cardToAdd;
    }


    public <T extends PhysicalCard> T addCardInHand(String blueprintId, String cardTitle, String ownerName,
                                                    Class<T> clazz) throws CardNotFoundException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.HAND, null);
        assertTrue(cardToAdd.isInHand(_game));
        assertFalse(cardToAdd.isInPlay());
        return cardToAdd;
    }

    public PhysicalCard addSeedCardUnderMission(String blueprintId, String cardTitle, String ownerName,
                                                MissionCard mission) throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, PhysicalCard.class);
        cardToAdd.setZone(Zone.VOID);
        GameLocation location = mission.getGameLocation(_game);
        if (location instanceof MissionLocation missionLocation) {
            missionLocation.seedCardUnderMission(_game, cardToAdd);
            return cardToAdd;
        } else {
            throw new CardNotFoundException("Could not find a mission location for mission card '" + mission.getBlueprintId() + "'");
        }
    }

    public <T extends ReportableCard> T addCardAboardShipOrFacility(String blueprintId, String cardTitle, String ownerName,
                                                                   CardWithCrew cardWithCrew, Class<T> clazz)
            throws CardNotFoundException, InvalidGameOperationException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);

        ReportCardAction reportAction = new ReportCardAction(_game, cardToAdd, false, cardWithCrew);

        if (cardToAdd instanceof AffiliatedCard affiliatedCard) {
            reportAction.setAffiliation(affiliatedCard.getCurrentAffiliations().getFirst());
        }
        executeAction(reportAction);

        assertTrue(cardToAdd.isInPlay());
        assertTrue(cardToAdd.isAttachedTo(cardWithCrew));
        assertTrue(cardWithCrew.hasCardInCrew(cardToAdd));
        return cardToAdd;
    }

    public ShipCard addDockedShip(String blueprintId, String cardTitle, String ownerName, FacilityCard facility)
            throws CardNotFoundException, InvalidGameOperationException {
        ShipCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, ShipCard.class);

        ReportCardAction reportAction = new ReportCardAction(_game, cardToAdd, false, facility);
        reportAction.setAffiliation(cardToAdd.getCurrentAffiliations().getFirst());
        executeAction(reportAction);

        assertTrue(cardToAdd.isInPlay());
        assertTrue(cardToAdd.isDocked());
        assertTrue(cardToAdd.isAttachedTo(facility));
        return cardToAdd;
    }

    public PhysicalCard addSeedDeckCard(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.SEED_DECK, null);
        return cardToAdd;
    }

    public <T extends PhysicalCard> T addSeedDeckCard(String blueprintId, String cardTitle, String ownerName, Class<T> clazz)
            throws CardNotFoundException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.SEED_DECK, null);
        return cardToAdd;
    }


    public PhysicalCard addDrawDeckCard(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.DRAW_DECK, null);
        return cardToAdd;
    }


    public MissionCard addMissionToDeck(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        MissionCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, MissionCard.class);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.MISSIONS_PILE, null);
        return cardToAdd;
    }

    public PhysicalCard addCardToCoreAsSeeded(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);

        SeedCardAction seedAction = new SeedCardAction(_game, cardToAdd, Zone.CORE);
        executeAction(seedAction);

        assertTrue(cardToAdd.isInPlay());
        return cardToAdd;
    }

    public PhysicalCard addCardToTopOfDiscard(String blueprintId, String cardTitle, String ownerName) throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.addCardToTopOfDiscardPile(cardToAdd);
        return cardToAdd;
    }
}