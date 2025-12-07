package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ST1EGameStateDeserializer extends JsonDeserializer<ST1EGameState> {

    private final ST1EGame _cardGame;
    private final CardBlueprintLibrary _blueprintLibrary;

    public ST1EGameStateDeserializer(ST1EGame cardGame, CardBlueprintLibrary library) {
        _cardGame = cardGame;
        _blueprintLibrary = library;
    }


    @Override
    public ST1EGameState deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);

        Phase currentPhase = readPropertyDirectly(object, ctxt, "currentPhase", Phase.class);
        GameProcess currentProcess = readPropertyDirectly(object, ctxt, "currentProcess", GameProcess.class);
        PlayerClock[] playerClocks = readPropertyDirectly(object, ctxt, "playerClocks", PlayerClock[].class);
        PlayerOrder playerOrder = readPropertyDirectly(object, ctxt, "playerOrder", PlayerOrder.class);

        List<PhysicalCard> cardsInGame = getCardsInGame(object, ctxt);

    /* still need to process:
        players
        awayTeams
        spacelineLocations - broken
        actions (equivalent to ActionsEnvironment)
        pendingDecision (this is not included in the non-player-specific version, but do we need it to capture playerDecisions?)
     */

    /*
        ST1EGameState properties not in the serialized game state:
    private int _nextAttemptingUnitId = 1;
    private int _nextLocationId = 1;
    private final ModifiersLogic _modifiersLogic;
    final List<PhysicalCard> _inPlay = new LinkedList<>();
    private int _currentTurnNumber;

     */

    /* ignore properties in gamestate:
        requestingPlayer
        phasesInOrder
        performedActions
        channelNumber
        timeStamp
     */


            // this constructor also sets the nextCardId
        return new ST1EGameState(currentPhase, currentProcess, playerClocks, cardsInGame, playerOrder);
    }

    private <T> T readPropertyDirectly(JsonNode object, DeserializationContext context, String propertyName,
                                       Class<T> clazz) throws IOException {
        try {
            JsonNode phaseNode = object.get(propertyName);
            return context.readTreeAsValue(phaseNode, clazz);
        } catch(IOException exp) {
            String fullMessage = "Unable to read property '" + propertyName + "'. " + exp.getMessage();
            throw new IOException(fullMessage);
        }
    }

    private List<PhysicalCard> getCardsInGame(JsonNode object, DeserializationContext ctxt) throws IOException {
        boolean deserializeDirectly = true;

        List<PhysicalCard> cardsInGame = new ArrayList<>();

        // skipped locationId
        if (deserializeDirectly) {
            JsonNode cardNode = object.get("cardsInGame");
            for (JsonNode individualCardNode : cardNode) {
                try {
                    CardType cardType = ctxt.readTreeAsValue(individualCardNode.get("cardType"), CardType.class);
                    Class<? extends ST1EPhysicalCard> clazz = switch(cardType) {
                        case EQUIPMENT -> EquipmentCard.class;
                        case FACILITY -> FacilityCard.class;
                        case MISSION -> MissionCard.class;
                        case PERSONNEL -> PersonnelCard.class;
                        case SHIP -> ShipCard.class;
                        case ARTIFACT, DILEMMA, DOORWAY, EVENT, INCIDENT, INTERRUPT, OBJECTIVE, SITE, TACTIC,
                                TIME_LOCATION, TRIBBLE, TROUBLE -> ST1EPhysicalCard.class;
                    };
                    ST1EPhysicalCard card = ctxt.readTreeAsValue(individualCardNode, clazz);
                    cardsInGame.add(card);

                    int cardId = individualCardNode.get("cardId").intValue();
                    String playerName = individualCardNode.get("owner").textValue();
                    String blueprintId = individualCardNode.get("blueprintId").textValue();

                    PhysicalCard testCard = _cardGame.createPhysicalCard(blueprintId, cardId, playerName);
                    if (card.getClass() != testCard.getClass()) {
                        throw new InvalidCardDefinitionException("Logic for deserializing card objects has changed");
                    }
                } catch(IOException exp) {
                    System.out.println(individualCardNode);
                    throw(exp);
                }
            }
        } else {
            JsonNode cardNode = object.get("cardsInGame");
            for (JsonNode individualCardNode : cardNode) {
                String ownerName = individualCardNode.get("owner").textValue();
                int cardId = individualCardNode.get("cardId").intValue();
                String blueprintId = individualCardNode.get("blueprintId").textValue();
                PhysicalCard card = _cardGame.createPhysicalCard(blueprintId, cardId, ownerName);
                cardsInGame.add(card);
                /* this definition doesn't include imageUrl, affiliation, locationId, attachedToCardId,
                    dockedAtCardId, stackedOnCardId, rangeAvailable, or isStopped
                */
            }
        }

        /* other properties from PhysicalCard classes
    protected Zone _zone;
    private boolean _placedOnMission = false;
    private boolean _revealedSeedCard = false;
    protected List<Affiliation> _currentAffiliations = new ArrayList<>();
    private Affiliation _defaultCardArtAffiliation;
    private int _hullIntegrity = 100;
    private boolean _docked = false;
    int _usedRange;
         */

        return cardsInGame;

    }

    private void getMissionStuff() {
/*        MissionLocation[] locations = new MissionLocation[0];
        JsonNode spacelineNode = object.get("spacelineLocations");
        try {
            locations = ctxt.readTreeAsValue(spacelineNode, MissionLocation[].class);
        } catch(IOException exp) {
            System.out.println(spacelineNode);
            System.out.println("Unable to deserialize spaceline locations");
            throw(exp);ex
        } */

        /*
            "locationId": 1,
            "quadrant": "ALPHA", // ALPHA, DELTA, GAMMA, MIRROR
            "locationName": "Gravesworld", // There may be multiple locations with the same name if universal
            "isCompleted": false, // Will show as true if the mission has been completed by either player
            "isHomeworld": false,
            "missionCardIds": [4,18], /* Card IDs for the cards representing this location. Typically this will be a
                                        list with one item, but for shared missions, there will be two. The first
                                        item in the list is the mission card on the bottom.
            "seedCardCount": 2,
                "seedCardIds": [27,28] // This property is not shown in player-specific game states

         */

        /* properties in MissionLocation:
            private static final Logger LOGGER = LogManager.getLogger(MissionLocation.class);
    @JsonProperty("quadrant")
    private final Quadrant _quadrant;
    @JsonProperty("region")
    private final Region _region;
    @JsonProperty("locationName")
    private final String _locationName;
    @JsonProperty("isCompleted")
    private boolean _isCompleted;
    @JsonProperty("locationId")
    private final int _locationId;
    private final CardPile<MissionCard> _missionCards = new CardPile<>();
    private final Map<String, CardPile<PhysicalCard>> _preSeedCards = new HashMap<>();
    private final CardPile<PhysicalCard> _seedCards;

         */


    }
}