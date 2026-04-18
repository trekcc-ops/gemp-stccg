import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { get_your_player_id, get_opponent_player_id } from "./common.jsx";
import { border, padding } from "@mui/system";

function result_text(gamestate) {
    switch(gamestate.endGameResult.reason) {
        case "ALL_PLAYERS_CANCELLED": {
            return {
                title: "Cancelled",
                description: "All players cancelled."
            };
        }
        case "CONCEDED": {
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return {
                        title: "Victory",
                        description: `${get_opponent_player_id(gamestate)} conceded.`
                    };
                }
                else {
                    return {
                        title: "Defeat",
                        description: `You conceded.`
                    };
                }
            }
            else {
                console.error("EndGameResult: Concession with no winner probably shouldn't happen...");
                return {
                    title: "Game Over",
                    description: `Concession. No winner.`
                };
            }
        }
        case "DECISION_TIMEOUT": {
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return {
                        title: "Victory",
                        description: `${get_opponent_player_id(gamestate)} did not make a decision in time.`
                    };
                }
                else {
                    return {
                        title: "Defeat",
                        description: "You did not make a decision in time."
                    }
                }
            }
            else {
                console.error("EndGameResult: Decision timeout with no winner probably shouldn't happen...");
                return {
                    title: "Game Over",
                    description: "Decision timeout. No winner."
                };
            }
        }
        case "ERROR": {
            return {
                title: "Game Over",
                description: "A critical error occurred."
            };
        }
        case "LAST_PLAYER_REMAINING": {
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return {
                        title: "Victory",
                        description: "You are the last player remaining."
                    };
                }
                else {
                    return {
                        title: "Victory",
                        description: `${get_opponent_player_id(gamestate)} is the last player remaining.`
                    };
                }
            }
            else {
                console.error("EndGameResult: Last player with no winner probably shouldn't happen...");
                return {
                    title: "Game Over",
                    description: "Last player remaining. No winner."
                };
            }
        }
        case "PLAYER_TIMEOUT": {
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    return {
                        title: "Victory",
                        description: `${get_opponent_player_id(gamestate)} timed out.`
                    };
                }
                else {
                    return {
                        title: "Defeat",
                        description: "You timed out."
                    };
                }
            }
            else {
                console.error("EndGameResult: Player timeout with no winner probably shouldn't happen...");
                return {
                    title: "Game Over",
                    description: "Player timeout. No winner."
                };
            }
        }
        case "TIE": {
            return {
                title: "Tie",
                description: ""
            };
        }
        case "WINNING_SCORE": {
            let oppId = get_opponent_player_id(gamestate);
            let oppData = gamestate.playerMap[oppId];
            let oppScore = oppData["points"]["towardWinning"];

            let yourId = get_your_player_id(gamestate);
            let yourData = gamestate.playerMap[yourId];
            let yourScore = yourData["points"]["towardWinning"];
            
            if (gamestate.endGameResult.winnerName) {
                if (gamestate.endGameResult.winnerName == get_your_player_id(gamestate)) {
                    // assumes a 2 player game
                    return {
                        title: "Victory",
                        description: `Final score: ${yourScore}-${oppScore}`
                    };
                }
                else {
                    // assumes a 2 player game
                    return {
                        title: "Defeat",
                        description: `${oppId} wins. Final score: ${yourScore}-${oppScore}.`
                    };
                }
            }
            else {
                console.error("EndGameResult: Winning score with no winner probably shouldn't happen...");
                return {
                    title: "Game Over",
                    description: `Winning score. ${yourScore}-${oppScore}. No winner.`
                };
            }
        }
        default: {
            return {
                title: "Game Over",
                description: ""
            };
        }
    }
}

export default function EndGameResult({gamestate}) {
    let result = result_text(gamestate);
    return(
        <Paper elevation={4} sx={{
            padding: "20px"
        }}>
            <Stack direction={"column"}>
                <Typography variant="h4" align='center'>{result.title}</Typography>
                <Typography align='center' sx={{paddingTop: "1em"}}>{result.description}</Typography>
            </Stack>
        </Paper>
    );
}