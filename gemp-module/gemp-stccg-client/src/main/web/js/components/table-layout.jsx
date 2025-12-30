import Stack from '@mui/material/Stack';
import SpacelineLocation from './spaceline-location.jsx';
import CardStack from './card-stack.jsx';

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

function get_core_cards(gamestate, playerid) {
    let retarr = [];

    let ids_in_core = [];
    for (const playerData of gamestate["players"]) {
        if (playerData["playerId"] === playerid) {
            for (const cardid of playerData["cardGroups"]["CORE"]["cardIds"]) {
                ids_in_core.push(cardid.toString());
            }
        }
    }

    let visible_cards_in_game = gamestate["visibleCardsInGame"];
    for (const cardid of ids_in_core) {
        if (Object.hasOwn(visible_cards_in_game, cardid.toString())) {
            const cardData = visible_cards_in_game[cardid.toString()];
            if (cardData.owner === playerid &&
                cardData.isInPlay === true) {
                
                retarr.push(cardData);
            }
        }
    }

    return retarr;
}

export default function TableLayout({gamestate}) {
    let opponentPlayerId = get_opponent_player_id(gamestate);
    let yourPlayerId = get_your_player_id(gamestate);
    let opponentCoreCards = get_core_cards(gamestate, opponentPlayerId);
    let yourCoreCards = get_core_cards(gamestate, yourPlayerId);

    let spacelineLocations = [];
    gamestate["spacelineLocations"].map((item, index) => {
        if (index === 0) {
            spacelineLocations.push(<SpacelineLocation key={item["locationId"]} gamestate={gamestate} locationid={item["locationId"]} showCore={true} />);
        }
        else {
            spacelineLocations.push(<SpacelineLocation key={item["locationId"]} gamestate={gamestate} locationid={item["locationId"]} />);
        }
    });


    return(
        <Stack id="table" direction="column">
            <Stack id="opponent-core" spacing={2} direction="row" sx={{
                alignItems: "center",
                justifyContent: "center"
            }}>
                {opponentCoreCards.map((cardData, index) => 
                    <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} sx={{transform: "rotate(180deg)"}} />
                )}
            </Stack>
            <Stack id="space"
                direction="row-reverse"
            >
                {spacelineLocations}
            </Stack>
            <Stack id="your-core" spacing={2} direction="row" sx={{
                alignItems: "center",
                justifyContent: "center"
            }}>
                {yourCoreCards.map((cardData, index) => 
                    <CardStack key={cardData.cardId} gamestate={gamestate} anchor_id={cardData.cardId} />
                )}
            </Stack>
        </Stack>
    );
}