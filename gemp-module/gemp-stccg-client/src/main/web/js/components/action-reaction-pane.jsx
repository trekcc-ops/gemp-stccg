import { Box, Typography } from "@mui/material";

function get_decision_text(gamestate) {
    let player_id = gamestate["requestingPlayer"];
    let current_player = gamestate["playerOrder"]["currentPlayer"];

    if (current_player !== player_id) {
        return `Waiting for ${current_player}`;
    }
    else {
        // It's your turn
        let decision_text = gamestate["pendingDecision"]["text"];
        if (decision_text !== null) {
            return decision_text;
        }
        else {
            // Odd, there should probably be some text.
            return "Waiting for you.";
        }
    }
}

export default function ActionReactionPane({gamestate, sx}) {
    return(
        <Box sx={{...sx}}>
            <Typography align='center'>{get_decision_text(gamestate)}</Typography>
        </Box>
    );
}