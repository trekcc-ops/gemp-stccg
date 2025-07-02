import Box from '@mui/material/Box';
import { Stack } from '@mui/material';
import Card from './card.jsx';

function get_hand(gamestate) {
    let player_id = gamestate["requestingPlayer"];
    let player_data = gamestate["players"].filter((data) => data["playerId"] === player_id);
    if (player_data.length !== 1) {
        console.error(`player with id ${player_id} not found`);
    }
    else {
        let ids_in_hand = player_data[0]["cardGroups"]["HAND"]["cardIds"];
        // DEBUG: console.log(ids_in_hand);
        let resolved_cards = [];
        let visible_cards_in_game = gamestate["visibleCardsInGame"];
        for (const cardid of ids_in_hand) {
            if (Object.hasOwn(visible_cards_in_game, cardid)) {
                resolved_cards.push(visible_cards_in_game[cardid]);
            }
            else {
                console.error(`Could not find card matching id ${cardid}`);
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