// Represents a column of cards anchored at a mission card.

import { Stack } from "@mui/material";
import CardStack from "./card-stack.jsx";

function get_your_player_id(gamestate) {
    return gamestate["requestingPlayer"];
}

function get_opponent_player_id(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    let opponent_player_data = gamestate["players"].filter((data) => data["playerId"] != your_player_id);
    return opponent_player_data[0]["playerId"];
}

function get_spaceline_location_data(gamestate, locationid) {
    for (const spacelineLocation of gamestate["spacelineLocations"]) {
        if (spacelineLocation["locationId"] == locationid) {
            return spacelineLocation;
        }
    }

    console.error(`Unable to load spaceline location data: ${locationid}`);
}

function get_mission_cards(gamestate, locationData) {
    let retarr = [];
    
    for (const cardId of locationData["missionCardIds"]) {
        let cardData = gamestate["visibleCardsInGame"][cardId.toString()];
        if (cardData != null) {
            retarr.push(<CardStack key={cardId} gamestate={gamestate} anchor_id={cardId} />); 
        }
    }
    return retarr;
}

function get_ship_cards(gamestate, locationid, playerid) {
    let retarr = [];
    
    for (const [cardId, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if (cardData["locationId"] === locationid && // at this location
            cardData["owner"] === playerid &&
            cardData["cardType"] === "SHIP" && // TODO: should probably also include self-controlling dilemmas
            cardData["attachedToCardId"] == null) // not docked
            {
            
            retarr.push(<CardStack key={cardId} gamestate={gamestate} anchor_id={cardData.cardId} />); 
        }
    }
    return retarr;
}

function get_facility_cards(gamestate, locationid, playerid) {
    let retarr = [];
    
    for (const [cardId, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if (cardData["locationId"] === locationid && // at this location
            cardData["owner"] === playerid &&
            cardData["cardType"] === "FACILITY" &&
            cardData["attachedToCardId"] == null) // not docked
            {
            
            retarr.push(<CardStack key={cardId} gamestate={gamestate} anchor_id={cardData.cardId} />); 
        }
    }
    return retarr;
}

export default function SpacelineLocation( {gamestate, locationid} ) {
    let yourPlayerId = get_your_player_id(gamestate);
    let opponentPlayerId = get_opponent_player_id(gamestate);
    let locationData = get_spaceline_location_data(gamestate, locationid);
    let missionCards = get_mission_cards(gamestate, locationData);
    let opponentShipCards = get_ship_cards(gamestate, locationid, opponentPlayerId);
    let yourShipCards = get_ship_cards(gamestate, locationid, yourPlayerId);
    let opponentFacilityCards = get_facility_cards(gamestate, locationid, opponentPlayerId);
    let yourFacilityCards = get_facility_cards(gamestate, locationid, yourPlayerId);

    return(
        <Stack
            data-spacelinelocation={locationid}
            flexGrow={1}
            direction="column"
            spacing={2}
            sx={{
                justifyContent: "space-between",
                alignItems: "center"
            }}
        >
            {opponentShipCards}
            {opponentFacilityCards}
            {missionCards}
            {yourShipCards}
            {yourFacilityCards}
        </Stack>
    );
}