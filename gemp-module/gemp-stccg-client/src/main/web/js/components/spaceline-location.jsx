// Represents a column of cards anchored at a mission card.

import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import CardStack from "./card-stack.jsx";
import CoreCardStack from './core-card-stack.jsx';

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

function get_quadrant_color(gamestate, locationId) {
    let locationData;
    for (let spacelineLocation of Object.values(gamestate["spacelineLocations"])) {
        if (spacelineLocation.locationId === locationId) {
            locationData = spacelineLocation;
        }
    }

    switch (locationData.quadrant) {
        case "ALPHA":
            return "rgba(21, 24, 119, 0.2)";
        case "GAMMA":
            return "rgba(21, 119, 21, 0.2)";
        case "DELTA":
            return "rgba(255, 255, 0, 0.2)";
        case "MIRROR":
            return "rgba(142, 29, 29, 0.2)";
        default:
            return "rgba(0, 0, 0, 0.2)";
    }
    
}

function get_region_border(gamestate, locationId) {
    let prevLocationData;
    let thisLocationData;
    let nextLocationData;
    for (let index = 0; index < gamestate["spacelineLocations"].length; index++) {
        const prevLocation = gamestate["spacelineLocations"][index - 1];
        const thisLocation = gamestate["spacelineLocations"][index];
        const nextLocation = gamestate["spacelineLocations"][index + 1];

        if (thisLocation.locationId === locationId) {
            thisLocationData = thisLocation;

            if (prevLocation) {
                prevLocationData = prevLocation;
            }

            if (nextLocation) {
                nextLocationData = nextLocation;
            }
            break;
        }
    }
    
    let prevBorderObj = {};
    let nextBorderObj = {};
    if (thisLocationData.region) {
        if (prevLocationData) {
            if (prevLocationData.region !== thisLocationData.region) {
                // rightmost in region
                prevBorderObj = {
                    borderRightStyle: "dashed",
                    borderRightWidth: "2px",
                    borderRightColor: "white"
                }
            }
        }

        if (nextLocationData) {
            if (nextLocationData.region !== thisLocationData.region) {
                // leftmost in region
                nextBorderObj = {
                    borderLeftStyle: "dashed",
                    borderLeftWidth: "2px",
                    borderLeftColor: "white"
                }
            }
        }
    }
    return {...prevBorderObj, ...nextBorderObj};
}

export default function SpacelineLocation( {gamestate, locationid, showCore = false} ) {
    let yourPlayerId = get_your_player_id(gamestate);
    let opponentPlayerId = get_opponent_player_id(gamestate);
    let locationData = get_spaceline_location_data(gamestate, locationid);
    let showCoreCards=false;
    const quadrantColor = get_quadrant_color(gamestate, locationid);
    const regionBorders = get_region_border(gamestate, locationid);

    // Render top to bottom
    let opponentCoreCards;
    /* Disable showing core cards in the spaceline since we made a special row for them.
    // TODO: Decide if we keep this
    if (showCoreCards) {
        opponentCoreCards = <CoreCardStack key={`core-${opponentPlayerId}`} gamestate={gamestate} player_id={opponentPlayerId} sx={{transform: "rotate(180deg)"}} />
    }
    */

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

    let yourCoreCards;
    /* Disable showing core cards in the spaceline since we made a special row for them.
    // TODO: Decide if we keep this
    if (showCoreCards) {
        yourCoreCards = <CoreCardStack key={`core-${yourPlayerId}`} gamestate={gamestate} player_id={yourPlayerId} />
    }
    */

    return(
        <Box
            data-spacelinelocation={locationid}
            flex={1}
            sx={{
                display: "grid",
                gridTemplateColumns: "1fr",
                gridTemplateRows: `[opp-side] minmax(auto, 1fr) [missions] auto [you-side] minmax(auto, 1fr)`,
                justifyItems: "center",
                backgroundColor: quadrantColor,
                ...regionBorders
            }}
        >   
            <Stack data-side={"opponentSide"} direction="column" alignItems={"flex-end"} justifyContent={"center"} sx={{gridRowStart: "opp-side"}}>
                {/* TODO: Decide if we keep core here or in a row */}
                <Stack id={"opponentCore"} direction="row" alignItems={"flex-end"} justifyContent={"center"} >
                    {opponentCoreCards}
                </Stack>
                {/* TODO: Time locations */}
                <Stack direction="row" sx={{transform: "rotate(180deg)"}}>
                    {opponentFacilityCards}
                </Stack>
                <Stack direction="row" sx={{transform: "rotate(180deg)"}}>
                    {opponentShipCards}
                </Stack>
            </Stack>

            <Stack direction="row" sx={{gridRowStart: "missions"}}>
                {missionCards}
            </Stack>

            <Stack data-side={"yourSide"} direction="column" alignItems={"flex-start"} justifyContent={"center"} sx={{gridRowStart: "you-side"}}>
                <Stack direction="row">
                    {yourShipCards}
                </Stack>
                <Stack direction="row">
                    {yourFacilityCards}
                </Stack>
                {/* TODO: Time locations */}
                {/* TODO: Decide if we keep core here or in a row */}
                <Stack id={"yourCore"} direction="row" alignItems={"flex-end"} justifyContent={"center"} >
                    {yourCoreCards}
                </Stack>
            </Stack>

            
        </Box>
    );
}