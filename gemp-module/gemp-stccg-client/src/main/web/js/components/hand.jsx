import Box from '@mui/material/Box';
import { Stack } from '@mui/material';
import Card from './card.jsx';

function get_hand(gamestate) {
    let player_id = gamestate["requestingPlayer"];
    let player_data = gamestate["players"].filter((data) => data["playerId"] === player_id);
    if (player_data.length != 1) {
        console.error(`player with id ${player_id} not found`);
    }
    else {
        let ids_in_hand = player_data[0]["cardGroups"]["HAND"]["cardIds"];
        // DEBUG: console.log(ids_in_hand);
        let resolved_cards = [];
        for (const cardid of ids_in_hand) {
            let matching_cards = Object.values(gamestate["visibleCardsInGame"]).filter((card) => card["cardId"] === cardid);
            if (matching_cards.length > 1) {
                console.error(`More than 1 card matching id ${cardid}`);
            }
            else if (matching_cards.length === 0) {
                console.error(`Could not find card matching id ${cardid}`);
            }
            else {
                resolved_cards.push(matching_cards[0]);
            }
        }
        // DEBUG: console.log(resolved_cards);
        return resolved_cards;
    }
}

export default function Hand( {gamestate} ) {
    let hand_from_gamestate = get_hand(gamestate);
    let rows = hand_from_gamestate.map(card => 
        <Card key={card["cardId"]} card={card} />
    )
    return(
        <Stack direction={"row"}>
            {rows}
        </Stack>);
}