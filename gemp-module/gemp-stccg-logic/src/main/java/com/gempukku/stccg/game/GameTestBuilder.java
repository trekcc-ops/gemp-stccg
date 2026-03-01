package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedFacilityAction;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.ChildCardRelationshipType;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.st1e.*;

import java.util.*;

public class GameTestBuilder {

    List<MissionCard> _missions = new ArrayList<>();
    private static final String DEFAULT_MISSION = "101_174";
    private static final String DEFAULT_MISSION_TITLE = "Khitomer Research";
    private final ST1EGame _game;
    private final Map<String, CardDeck> _decks = new HashMap<>();
    private Phase _startingPhase;
    private final List<String> _players;
    private boolean _skipStartingHands;
    private final Collection<StoppableCard> _cardsToStop = new ArrayList<>();
    private final String _firstPlayerName;
    private final String _gameName;

    @JsonCreator
    private GameTestBuilder(@JsonProperty("gameName") String gameName,
                            @JsonProperty("players") List<String> players,
                            @JsonProperty("phase") Phase startingPhase,
                            @JsonProperty("missions") List<JsonNode> missions,
                            @JsonProperty("seedDeck") List<List<String>> seedDecks,
                            @JsonProperty("drawDeck") List<List<String>> drawDecks,
                            @JsonProperty("core") List<List<String>> cores,
                            @JsonProperty("hand") List<List<String>> hands,
                            @JacksonInject FormatLibrary formatLibrary,
                            @JacksonInject CardBlueprintLibrary cardLibrary
                            ) throws InvalidGameOperationException, CardNotFoundException {
        _gameName = gameName;
        GameFormat format = formatLibrary.get("st1emoderncomplete");
        CardDeck testDeck = new CardDeck("Test", format);
        for (int i = 0; i < 30; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "991_001");
        }

        for (String player : players) {
            _decks.put(player, testDeck);
        }
        _game = new ST1EGame(format, _decks, cardLibrary, GameTimer.GLACIAL_TIMER, new GameRandomizer());
        _startingPhase = Phase.SEED_DOORWAY;
        _players = players;
        _firstPlayerName = players.getFirst();
        if (startingPhase != null) {
            setPhase(startingPhase);
        }
        if (seedDecks != null) {
            for (int i = 0; i < seedDecks.size(); i++) {
                List<String> seedDeckList = seedDecks.get(i);
                for (String blueprintId : seedDeckList) {
                    CardBlueprint blueprint = cardLibrary.get(blueprintId);
                    if (blueprint != null) {
                        addSeedDeckCard(blueprintId, blueprint.getTitle(), _players.get(i));
                    }
                }
            }
        }
        if (drawDecks != null) {
            for (int i = 0; i < drawDecks.size(); i++) {
                List<String> drawDeckList = drawDecks.get(i);
                for (String blueprintId : drawDeckList) {
                    CardBlueprint blueprint = cardLibrary.get(blueprintId);
                    if (blueprint != null) {
                        addDrawDeckCard(blueprintId, blueprint.getTitle(), _players.get(i));
                    }
                }
            }
        }
        if (cores != null) {
            for (int i = 0; i < cores.size(); i++) {
                List<String> coreList = cores.get(i);
                for (String blueprintId : coreList) {
                    CardBlueprint blueprint = cardLibrary.get(blueprintId);
                    if (blueprint != null) {
                        addCardToCoreAsSeeded(blueprintId, blueprint.getTitle(), _players.get(i));
                    }
                }
            }
        }
        if (hands != null) {
            for (int i = 0; i < hands.size(); i++) {
                List<String> handList = hands.get(i);
                for (String blueprintId : handList) {
                    CardBlueprint blueprint = cardLibrary.get(blueprintId);
                    if (blueprint != null) {
                        addCardInHand(blueprintId, blueprint.getTitle(), _players.get(i));
                    }
                }
            }
        }
        addJsonCards(cardLibrary, missions);
    }

    public GameTestBuilder(CardBlueprintLibrary cardBlueprintLibrary, FormatLibrary formatLibrary,
                           List<String> playerNames) throws InvalidGameOperationException {
        this(cardBlueprintLibrary, formatLibrary, playerNames, new GameRandomizer());
    }

    public GameTestBuilder(CardBlueprintLibrary cardBlueprintLibrary, FormatLibrary formatLibrary,
                           List<String> playerNames, GameRandomizer randomizer) throws InvalidGameOperationException {
        _gameName = "Test";
        GameFormat format = formatLibrary.get("st1emoderncomplete");
        CardDeck testDeck = new CardDeck("Test", format);
        for (int i = 0; i < 30; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "991_001"); // dummy 1E dilemma
        }

        for (String player : playerNames) {
            _decks.put(player, testDeck);
        }
        _game = new ST1EGame(format, _decks, cardBlueprintLibrary, GameTimer.GLACIAL_TIMER, randomizer);
        _startingPhase = Phase.SEED_DOORWAY;
        _players = playerNames;
        _firstPlayerName = playerNames.getFirst();
    }

    public ST1EGame initializeGame() throws InvalidGameOperationException {
        // Initialize player order
        if (_players.contains(_firstPlayerName)) {
            PlayerOrder playerOrder = new PlayerOrder(false, _firstPlayerName, _players);
            _game.getGameState().initializePlayerOrder(playerOrder);
        }

        if (!_startingPhase.isSeedPhase()) {
            // draw starting hand
            if (!_skipStartingHands) {
                GameProcess facilityProcess = new ST1EFacilitySeedPhaseProcess(_players.size());
                facilityProcess.getNextProcess(_game);
            } else {
                _game.getGameState().startPlayerTurn(_players.getFirst());
            }
            // start turn
            _game.getGameState().signalStartOfTurn(_game, _players.getFirst());
        }

        GameProcess currentProcess = switch(_startingPhase) {
            case END_OF_TURN -> new ST1EEndOfTurnProcess();
            case SEED_DOORWAY -> new DoorwaySeedPhaseProcess(_players);
            case SEED_MISSION -> new ST1EMissionSeedPhaseProcess(0);
            case SEED_DILEMMA -> new DilemmaSeedPhaseOpponentsMissionsProcess(_players);
            case SEED_FACILITY -> new ST1EFacilitySeedPhaseProcess(0);
            case CARD_PLAY, EXECUTE_ORDERS -> new ST1EPlayPhaseSegmentProcess(_game.getCurrentPlayerId());
            case BETWEEN_TURNS, TRIBBLES_TURN, START_OF_TURN -> throw new InvalidGameOperationException(
                    "Unequipped to create test game starting in phase '" + _startingPhase + "'");
        };

        if (!_cardsToStop.isEmpty()) {
            StopCardsAction stopAction = new StopCardsAction(_game, _players.getFirst(), _cardsToStop);
            executeAction(stopAction);
        }

        _game.getGameState().setCurrentProcess(currentProcess);
        _game.setCurrentPhase(_startingPhase);
        return _game;
    }

    public ST1EGame startGame() throws InvalidGameOperationException {
        initializeGame();
        _game.startGame();
        return _game;
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

    public MissionCard addMission(MissionType missionType, Affiliation affiliation, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        List<String> cardSpecs = (missionType == MissionType.PLANET) ?
                switch(affiliation) {
                    case FEDERATION, KLINGON -> List.of("113_003", "Botanical Research");
//                    case NON_ALIGNED -> List.of("155_038", "Encounter at Farpoint"); // not universal
                    case FERENGI -> List.of("113_006", "Search for Weapons");
                    case BAJORAN, CARDASSIAN, ROMULAN -> List.of("161_021", "Advanced Combat Training");
                    case BORG, DOMINION, HIROGEN, KAZON, NEUTRAL, NON_ALIGNED, STARFLEET, VIDIIAN, VULCAN, XINDI -> null;
                } :
                switch(affiliation) {
                    case CARDASSIAN, FERENGI, ROMULAN -> List.of("117_045", "Collect Sample");
                    case FEDERATION, KLINGON -> List.of("113_008", "Study Pulsar");
                    case BAJORAN, BORG, DOMINION, HIROGEN, KAZON, NEUTRAL, NON_ALIGNED, STARFLEET, VIDIIAN, VULCAN, XINDI -> null;
                };
        if (cardSpecs == null) {
            throw new CardNotFoundException("addMission does not have a default card for " + missionType + " and " + affiliation);
        }
        MissionCard mission = addCardToGame(cardSpecs.get(0), cardSpecs.get(1), ownerName, MissionCard.class);
        _missions.add(mission);
        SeedMissionCardAction seedAction = new SeedMissionCardAction(_game, mission, _missions.indexOf(mission));
        executeAction(seedAction);
        return mission;
    }


    public MissionCard addMission(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        MissionCard mission = addCardToGame(blueprintId, cardTitle, ownerName, MissionCard.class);
        _missions.add(mission);
        SeedMissionCardAction seedAction = new SeedMissionCardAction(_game, mission, _missions.indexOf(mission));
        executeAction(seedAction);
        return mission;
    }

    public ShipCard addShipInSpace(String shipBlueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        if (_missions.isEmpty()) {
            addMission(DEFAULT_MISSION, DEFAULT_MISSION_TITLE, ownerName);
        }
        MissionCard destination = _missions.getFirst();
        return addShipInSpace(shipBlueprintId, cardTitle, ownerName, destination);
    }


    public ShipCard addShipInSpace(String shipBlueprintId, String cardTitle, String ownerName, PhysicalCard mission)
            throws CardNotFoundException, InvalidGameOperationException {
        ShipCard shipCard = addCardToGame(shipBlueprintId, cardTitle, ownerName, ShipCard.class);
        ReportCardAction playAction = new ReportCardAction(_game, shipCard, true);
        playAction.setDestination(mission);
        playAction.setAffiliation(shipCard.getCurrentAffiliations().getFirst());
        executeAction(playAction);
        return shipCard;
    }

    public ReportableCard addCardOnPlanetSurface(String blueprintId, String cardTitle, String ownerName,
                                                 MissionCard mission)
            throws InvalidGameOperationException, CardNotFoundException {
        return addCardOnPlanetSurface(blueprintId, cardTitle, ownerName, mission, ReportableCard.class);
    }

    public <T extends ReportableCard> T addCardOnPlanetSurface(String blueprintId, String cardTitle, String ownerName,
                                               MissionCard mission, Class<T> clazz)
            throws CardNotFoundException, InvalidGameOperationException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
        GameLocation location = mission.getGameLocation(_game);
        if (!location.isPlanet()) {
            throw new InvalidGameOperationException("Location is not a planet");
        }
        ReportCardAction playAction = new ReportCardAction(_game, cardToAdd, true);
        playAction.setDestination(mission);
        if (cardToAdd instanceof AffiliatedCard affiliatedCard) {
            playAction.setAffiliation(affiliatedCard.getCurrentAffiliations().getFirst());
        }
        executeAction(playAction);
        if (!cardToAdd.isInPlay() || !cardToAdd.isAtSameLocationAsCard(mission) || !cardToAdd.isOnPlanet(_game)) {
            throw new InvalidGameOperationException("Did not achieve expected results from GameTestBuilder");
        } else {
            return cardToAdd;
        }
    }

    public FacilityCard addOutpost(Affiliation affiliation, String ownerName, MissionCard mission)
            throws CardNotFoundException, InvalidGameOperationException {
        String facilityBlueprintId = switch(affiliation) {
            case BAJORAN -> "112_078";
            case CARDASSIAN -> "112_080";
            case DOMINION -> "178_033";
            case FEDERATION -> "101_104";
            case FERENGI -> "117_030";
            case KLINGON -> "101_105";
            case NON_ALIGNED -> "111_009";
            case ROMULAN -> "101_106";
            case VULCAN -> "194_076";
            case BORG, HIROGEN, KAZON, NEUTRAL, STARFLEET, VIDIIAN, XINDI ->
                    throw new CardNotFoundException("Could not find blueprint for outpost of affiliation " + affiliation);
        };
        return addFacility(facilityBlueprintId, ownerName, mission);
    }

    public FacilityCard addOutpost(Affiliation affiliation, String ownerName)
            throws CardNotFoundException, InvalidGameOperationException {
        if (_missions.isEmpty()) {
            addMission(DEFAULT_MISSION, DEFAULT_MISSION_TITLE, ownerName);
        }
        return addOutpost(affiliation, ownerName, _missions.getFirst());
    }

    public FacilityCard addFacility(String facilityBlueprintId, String ownerName, MissionCard mission)
            throws CardNotFoundException, InvalidGameOperationException {
        PhysicalCard facilityCard = _game.addCardToGame(facilityBlueprintId, ownerName);
        if (facilityCard instanceof FacilityCard facility) {
            SeedFacilityAction seedAction = new SeedFacilityAction(_game, facility, new HashMap<>());
            seedAction.setDestination(mission);
            seedAction.setAffiliation(facility.getCurrentAffiliations().getFirst());
            executeAction(seedAction);
            return facility;
        } else {
            throw new CardNotFoundException("Card incorrect type: " + facilityCard.getClass().getSimpleName());
        }
    }


    public PhysicalCard addCardInHand(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.HAND, null);
        return cardToAdd;
    }

    public PhysicalCard addCardInDiscard(String blueprintId, String cardTitle, String ownerName)
            throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.DISCARD, null);
        return cardToAdd;
    }



    public <T extends PhysicalCard> T addCardInHand(String blueprintId, String cardTitle, String ownerName,
                                                    Class<T> clazz) throws CardNotFoundException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
        _game.getGameState().addCardToZone(_game, cardToAdd, Zone.HAND, null);
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

        ReportCardAction reportAction = new ReportCardAction(_game, cardToAdd, false);
        reportAction.setDestination(cardWithCrew);

        if (cardToAdd instanceof AffiliatedCard affiliatedCard) {
            reportAction.setAffiliation(affiliatedCard.getCurrentAffiliations().getFirst());
        }
        executeAction(reportAction);

        return cardToAdd;
    }

    public <T extends ReportableCard> T addCardAboardShipOrFacility(String blueprintId, String cardTitle, String ownerName,
                                                                    CardWithCrew cardWithCrew, Class<T> clazz, Affiliation affiliation)
            throws CardNotFoundException, InvalidGameOperationException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);

        ReportCardAction reportAction = new ReportCardAction(_game, cardToAdd, false);
        reportAction.setDestination(cardWithCrew);
        reportAction.setAffiliation(affiliation);

        executeAction(reportAction);

        return cardToAdd;
    }

    public ShipCard addDockedShip(String blueprintId, String cardTitle, String ownerName, FacilityCard facility)
            throws CardNotFoundException, InvalidGameOperationException {
        ShipCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, ShipCard.class);

        ReportCardAction reportAction = new ReportCardAction(_game, cardToAdd, false);
        reportAction.setDestination(facility);
        reportAction.setAffiliation(cardToAdd.getCurrentAffiliations().getFirst());
        executeAction(reportAction);

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

    public <T extends PhysicalCard> T addDrawDeckCard(String blueprintId, String cardTitle, String ownerName, Class<T> clazz)
            throws CardNotFoundException {
        T cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName, clazz);
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

        return cardToAdd;
    }

    public PhysicalCard addCardToTopOfDiscard(String blueprintId, String cardTitle, String ownerName) throws CardNotFoundException {
        PhysicalCard cardToAdd = addCardToGame(blueprintId, cardTitle, ownerName);
        _game.addCardToTopOfDiscardPile(cardToAdd);
        return cardToAdd;
    }

    public void skipStartingHands() {
        _skipStartingHands = true;
    }

    public void stopCard(StoppableCard cardToStop) {
        _cardsToStop.add(cardToStop);
    }

    private void addJsonCards(CardBlueprintLibrary library, List<JsonNode> jsonNodes)
            throws CardNotFoundException, InvalidGameOperationException {
        for (JsonNode node : jsonNodes) {
            String blueprintId = node.get("blueprintId").textValue();
            String ownerText = node.get("owner").textValue();
            String ownerName = switch(ownerText) {
                case "P1" -> _players.get(0);
                case "P2" -> _players.get(1);
                default -> ownerText;
            };
            CardBlueprint blueprint = library.getCardBlueprint(blueprintId);
            if (blueprint.getCardType() == CardType.MISSION) {
                MissionCard mission = addMission(blueprintId, blueprint.getTitle(), ownerName);
                if (node.has("IN_SPACE")) {
                    addJsonCards(library, node.get("IN_SPACE"), ChildCardRelationshipType.IN_SPACE, mission);
                }
            }
        }
    }

    private void addJsonCards(CardBlueprintLibrary library, JsonNode jsonNodes, ChildCardRelationshipType childType,
                              PhysicalCard parentCard)
            throws CardNotFoundException, InvalidGameOperationException {
        for (JsonNode node : jsonNodes) {
            String blueprintId = node.get("blueprintId").textValue();
            String ownerText = node.get("owner").textValue();
            String ownerName = switch(ownerText) {
                case "P1" -> _players.get(0);
                case "P2" -> _players.get(1);
                default -> ownerText;
            };
            CardBlueprint blueprint = library.getCardBlueprint(blueprintId);
            PhysicalCard cardToAdd = switch(childType) {
                case IN_SPACE -> addCardInSpace(blueprint, ownerName, parentCard);
                default -> throw new InvalidGameOperationException(
                        "GameTestBuilder is not yet equipped to handle ChildCardRelationshipType '" + childType + "'");
            };
        }
    }

    private PhysicalCard addCardInSpace(CardBlueprint cardBlueprint, String ownerName, PhysicalCard mission)
            throws InvalidGameOperationException, CardNotFoundException {
        if (!(mission instanceof MissionCard)) {
            throw new InvalidGameOperationException("Cannot add a card in space at non-mission card '" + mission.getTitle() + "'");
        }
        if (cardBlueprint.getCardType() == CardType.FACILITY) {
            return addFacility(cardBlueprint.getBlueprintId(), ownerName, (MissionCard) mission);
        } else if (cardBlueprint.getCardType() == CardType.SHIP) {
            return addShipInSpace(cardBlueprint.getBlueprintId(), cardBlueprint.getTitle(), ownerName, mission);
        } else {
            throw new InvalidGameOperationException(
                    "Cannot add '" + cardBlueprint.getTitle() + "' in space because it is not a ship or facility");
        }
    }

    public String getName() {
        return _gameName;
    }

}