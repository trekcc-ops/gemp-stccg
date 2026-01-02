import Box from '@mui/material/Box';
import { Stack } from '@mui/material';
import CardStack from './card-stack.jsx';

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

export default function Hand( {gamestate, openCardDetailsFunc, sx} ) {
    let hand_from_gamestate = get_hand(gamestate);
    let rows = hand_from_gamestate.map(card => 
        <CardStack key={card.cardId} gamestate={gamestate} anchor_id={card.cardId} openCardDetailsFunc={openCardDetailsFunc} />
    )
    return(
        <Stack direction={"row"} spacing={1} justifyContent={"center"} alignItems={"center"} sx={{...sx}}>
            {rows}
        </Stack>);
}