import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import { get_your_player_id, get_opponent_player_id } from "./common.jsx";

function result_text(gamestate) {
    switch(gamestate.endGameResult.reason) {
        case "ALL_PLAYERS_CANCELLED":
            return "All players cancelled.";
        case "CONCEDED": {
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return `${get_opponent_player_id(gamestate)} conceded.`;
                }
                else {
                    return "You conceded.";
                }
            }
            else {
                console.error("EndGameResult: Concession with no winner probably shouldn't happen...");
                return "Game Over, concession. No winner."
            }
        }
        case "DECISION_TIMEOUT":
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return `${get_opponent_player_id(gamestate)} did not make a decision in time.`;
                }
                else {
                    return "You did not make a decision in time.";
                }
            }
            else {
                console.error("EndGameResult: Decision timeout with no winner probably shouldn't happen...");
                return "Game Over, decision timeout. No winner."
            }
        case "ERROR":
            return "Game Over. A critical error occurred.";
        case "LAST_PLAYER_REMAINING":
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return "You are the last player remaining.";
                }
                else {
                    return `${get_opponent_player_id(gamestate)} is the last player remaining.`;
                }
            }
            else {
                console.error("EndGameResult: Last player with no winner probably shouldn't happen...");
                return "Game Over, last player remaining. No winner."
            }
        case "PLAYER_TIMEOUT":
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return `${get_opponent_player_id(gamestate)} timed out.`;
                }
                else {
                    return "You timed out.";
                }
            }
            else {
                console.error("EndGameResult: Player timeout with no winner probably shouldn't happen...");
                return "Game Over, player timeout. No winner."
            }
        case "TIE":
            return "Tie game."
        case "WINNING_SCORE":
            if (gamestate.endGameResult.winnerName) {
                let oppId = get_opponent_player_id(gamestate);
                let oppData = gamestate.playerMap[oppId];
                let oppScore = oppData["points"]["total"];

                let yourId = get_your_player_id(gamestate);
                let yourData = gamestate.playerMap[yourId];
                let yourScore = yourData["points"]["total"];

                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    // assumes a 2 player game
                    return `You win, ${yourScore}-${oppScore}.`;
                }
                else {
                    // assumes a 2 player game
                    return `${oppId} wins, ${yourScore}-${oppScore}.`;
                }
            }
            else {
                console.error("EndGameResult: Player timeout with no winner probably shouldn't happen...");
                return "Game Over, player timeout. No winner."
            }
        default:
            return "Game Over";
    }
}

export default function EndGameResult({gamestate}) {
    return(
        <Box>
            <Typography align='center'>{result_text(gamestate)}</Typography>
        </Box>
    );
}