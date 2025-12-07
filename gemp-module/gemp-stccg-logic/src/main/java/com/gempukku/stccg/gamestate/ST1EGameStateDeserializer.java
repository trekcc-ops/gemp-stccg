package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ST1EGameStateDeserializer extends JsonDeserializer<ST1EGameState> {

    private final ST1EGame _cardGame;

    public ST1EGameStateDeserializer(ST1EGame cardGame) {
        _cardGame = cardGame;
    }


    @Override
    public ST1EGameState deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        JsonNode phaseNode = object.get("currentPhase");
        Phase currentPhase = ctxt.readTreeAsValue(phaseNode, Phase.class);

        JsonNode processNode = object.get("currentProcess");
        GameProcess currentProcess = ctxt.readTreeAsValue(processNode, GameProcess.class);

        JsonNode clockNode = object.get("playerClocks");
        PlayerClock[] clocks = ctxt.readTreeAsValue(clockNode, PlayerClock[].class);
        List<PlayerClock> clockList = Arrays.stream(clocks).toList();

        JsonNode playerOrderNode = object.get("playerOrder");
        PlayerOrder playerOrder = ctxt.readTreeAsValue(playerOrderNode, PlayerOrder.class);


        boolean deserializeDirectly = false;

        List<PhysicalCard> cardsInGame = new ArrayList<>();

        if (deserializeDirectly) {
            JsonNode cardNode = object.get("cardsInGame");
            for (JsonNode individualCardNode : cardNode) {
                ST1EPhysicalCard card = ctxt.readTreeAsValue(individualCardNode, ST1EPhysicalCard.class);
                cardsInGame.add(card);
            }
        } else {
            JsonNode cardNode = object.get("cardsInGame");
            for (JsonNode individualCardNode : cardNode) {
                String ownerName = individualCardNode.get("owner").textValue();
                int cardId = individualCardNode.get("cardId").intValue();
                String blueprintId = individualCardNode.get("blueprintId").textValue();
                PhysicalCard card = _cardGame.createPhysicalCard(blueprintId, cardId, ownerName);
                cardsInGame.add(card);
            }
        }

        /* Other card properties in json not implemented here:
            imageUrl
            affiliation
            locationId (is a full GameLocation object in the Java)
            attachedToCardId
            dockedAtCardId
            stackedOnCardId
            rangeAvailable
            isStopped
         */

        /* other properties from PhysicalCard classes
    protected Zone _zone;
    private boolean _placedOnMission = false;
    private boolean _revealedSeedCard = false;
    protected List<Affiliation> _currentAffiliations = new ArrayList<>();
    private Affiliation _defaultCardArtAffiliation;
    private AwayTeam _awayTeam;
    private int _hullIntegrity = 100;
    private boolean _docked = false;
    int _usedRange;
         */



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


        return new ST1EGameState(currentPhase, currentProcess, clockList, cardsInGame, playerOrder);
    }

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

}