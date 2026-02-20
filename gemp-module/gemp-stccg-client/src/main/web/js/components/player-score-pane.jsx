import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';
import decipher_card_deck from '../../images/decipher_card_deck.svg?no-inline';
import decipher_card_discard from '../../images/decipher_card_discard.svg?no-inline';
import decipher_card_hand from '../../images/decipher_card_hand.svg?no-inline';
import decipher_card_removed from '../../images/decipher_card_removed.svg?no-inline';
import '../../css/player-score-pane.css';

function get_player_data(player_id, gamestate) {
    let player_data = gamestate.playerMap[player_id];
    if (player_data == null) {
        console.error(`player with id ${player_id} not found`);
        // 0 here hides the number badges
        return {
            "username": "",
            "drawsize": 0,
            "handsize": 0,
            "discardsize": 0,
            "removedsize": 0,
            "score": 0,
            "clock": "0:00"
        };
    }
    else {
        let username = player_data["playerId"];
        let drawsize = player_data["cardGroups"]["DRAW_DECK"]["cardCount"];
        let handsize = player_data["cardGroups"]["HAND"]["cardCount"];
        let discardsize = player_data["cardGroups"]["DISCARD"]["cardCount"];
        let removedsize = player_data["cardGroups"]["REMOVED"]["cardCount"];
        let thescore = player_data["score"];

        let player_clock;
        for (const clockObj of gamestate["playerClocks"]) {
            if (clockObj.playerId === player_id) {
                let date = new Date(0);
                date.setSeconds(parseInt(clockObj.timeRemaining));
                player_clock = date.toISOString().substring(11, 19);
            }
        }
        
        return {
            "username": username,
            "clock": player_clock,
            "drawsize": drawsize,
            "handsize": handsize,
            "discardsize": discardsize,
            "removedsize": removedsize,
            "score": thescore,
        };
    }
}

export default function PlayerScorePane ( {gamestate, player_id} ) {
    let badge_color = 'secondary';
    let player_data = get_player_data(player_id, gamestate);

    return(
        <Box className="PlayerScorePane">
            <Box className="PlayerName">
                <Typography>{player_data.username}</Typography>
            </Box>
            <Box>
                <Tooltip title="Remaining clock">
                    <Typography>{player_data.clock}</Typography>
                </Tooltip>
            </Box>
            <Box className="DrawDeckSize" sx={{backgroundImage: `url(${decipher_card_deck})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Draw deck size">
                    <Typography align='center'>{player_data.drawsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="HandSize" sx={{backgroundImage: `url(${decipher_card_hand})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Hand size">
                    <Typography align='center'>{player_data.handsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="DiscardSize" sx={{backgroundImage: `url(${decipher_card_discard})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Discard size">
                    <Typography align='center'>{player_data.discardsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="RemovedSize" sx={{backgroundImage: `url(${decipher_card_removed})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Removed from game">
                    <Typography align='center'>{player_data.removedsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="Score">
                <Typography align='center'>SCORE: {player_data.score}</Typography>
            </Box>
        </Box>
    );
}