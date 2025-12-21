// Represents a column of cards anchored at a mission card.

import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import CardStack from "./card-stack.jsx";

// TODO: Reuse the identical function from 1e-gamestate-layout.jsx.
function get_your_player_id(gamestate) {
    return gamestate["requestingPlayer"];
}

// TODO: Reuse the identical function from 1e-gamestate-layout.jsx.
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

function get_mission_cards(gamestate, locationData, yourPlayerId) {
    let retarr = [];
    
    for (const cardId of locationData["missionCardIds"]) {
        let cardData = gamestate["visibleCardsInGame"][cardId.toString()];
        if (cardData != null) {
            retarr.push(cardData);
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
            
            retarr.push(cardData); 
        }
    }
    return retarr;
}

function player_has_ship_cards_anywhere(gamestate, playerid) {
    let ships = Object.values(gamestate["visibleCardsInGame"])
        .filter((cardData) => (
            cardData["owner"] === playerid &&
            cardData["cardType"] === "SHIP" &&
            cardData["attachedToCardId"] == null
        ));
    
    return(ships.length > 0);
}

function get_facility_cards(gamestate, locationid, playerid) {
    let retarr = [];
    
    for (const [cardId, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if (cardData["locationId"] === locationid && // at this location
            cardData["owner"] === playerid &&
            cardData["cardType"] === "FACILITY" &&
            cardData["attachedToCardId"] == null) // not docked
            {
            
            retarr.push(cardData); 
        }
    }
    return retarr;
}

export default function SpacelineLocation( {gamestate, locationid} ) {
    let yourPlayerId = get_your_player_id(gamestate);
    let opponentPlayerId = get_opponent_player_id(gamestate);
    let locationData = get_spaceline_location_data(gamestate, locationid);

    // Render top to bottom
    let opponentFacilityCards = get_facility_cards(gamestate, locationid, opponentPlayerId).map((cardData, index) =>
        <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} />
    );
    let opponentShipCards = get_ship_cards(gamestate, locationid, opponentPlayerId).map((cardData, index) =>
        <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} />
    );
    let missionCards = get_mission_cards(gamestate, locationData, yourPlayerId).map((cardData, index) => {
        let isInverted = cardData.owner === yourPlayerId ? "none" : "rotate(180deg)";
        return(<CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} sx={{transform: isInverted}} />);
    });
    let yourShipCards = get_ship_cards(gamestate, locationid, yourPlayerId).map((cardData, index) =>
        <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} />
    );
    
    let yourFacilityCards = get_facility_cards(gamestate, locationid, yourPlayerId).map((cardData, index) =>
        <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} />
    );

    let opponentShipRowHeight = player_has_ship_cards_anywhere(gamestate, opponentPlayerId) ? "1fr" : "auto";
    let yourShipRowHeight = player_has_ship_cards_anywhere(gamestate, yourPlayerId) ? "1fr" : "auto";

    return(
        <Box
            data-spacelinelocation={locationid}
            flex={1}
            sx={{
                display: "grid",
                gridTemplateColumns: "1fr",
                gridTemplateRows: `[opp-special] auto [opp-facil] 1fr [opp-ship] ${opponentShipRowHeight} [missions] 1fr [you-ship] ${yourShipRowHeight} [you-facil] 1fr [you-special] auto`,
                justifyItems: "center",
                alignItems: "center"
            }}
        >
            <Stack direction="row" sx={{gridRowStart: "opp-facil", transform: "rotate(180deg)"}}>
                {opponentFacilityCards}
            </Stack>
            <Stack direction="row" sx={{gridRowStart: "opp-ship", transform: "rotate(180deg)"}}>
                {opponentShipCards}
            </Stack>
            <Stack direction="row" sx={{gridRowStart: "missions"}}>
                {missionCards}
            </Stack>
            <Stack direction="row" sx={{gridRowStart: "you-ship"}}>
                {yourShipCards}
            </Stack>
            <Stack direction="row" sx={{gridRowStart: "you-facil"}}>
                {yourFacilityCards}
            </Stack>
        </Box>
    );
}