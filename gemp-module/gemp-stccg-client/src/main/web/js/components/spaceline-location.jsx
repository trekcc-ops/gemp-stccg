// Represents a column of cards anchored at a mission card.

import { Stack } from "@mui/material";
import CardStack from "./card-stack.jsx";

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

function get_ship_cards(gamestate, locationid) {
    let retarr = [];
    
    for (const [cardId, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if (cardData["locationId"] === locationid && // at this location
            cardData["cardType"] === "SHIP" && // TODO: should probably also include self-controlling dilemmas
            cardData["attachedToCardId"] == null) // not docked
            {
            
            retarr.push(<CardStack key={cardId} gamestate={gamestate} anchor_id={cardData.cardId} />); 
        }
    }
    return retarr;
}

export default function SpacelineLocation( {gamestate, locationid} ) {
    let locationData = get_spaceline_location_data(gamestate, locationid);
    let missionCards = get_mission_cards(gamestate, locationData);
    let shipCards = get_ship_cards(gamestate, locationid);

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
            {missionCards}
            {shipCards}
        </Stack>
    );
}