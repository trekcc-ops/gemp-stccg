import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Grid from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import Badge from '@mui/material/Badge';
import Tooltip from '@mui/material/Tooltip';
import decipher_card_deck from '../../images/decipher_card_deck.svg?no-inline';
import decipher_card_discard from '../../images/decipher_card_discard.svg?no-inline';
import decipher_card_hand from '../../images/decipher_card_hand.svg?no-inline';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg?no-inline';
import '../../css/player-score-pane.css';

function get_player_data(player_id, gamestate) {
    let player_data = gamestate["players"].filter((data) => data["playerId"] === player_id);
    if (player_data.length != 1) {
        console.error(`player with id ${player_id} not found`);
        // 0 here hides the number badges
        return {
            "username": "",
            "drawsize": 0,
            "handsize": 0,
            "discardsize": 0,
            "removedsize": 0,
            "score": 0
        };
    }
    else {
        let username = player_data[0]["playerId"];
        let drawsize = player_data[0]["cardGroups"]["DRAW_DECK"]["cardCount"];
        let handsize = player_data[0]["cardGroups"]["HAND"]["cardCount"];
        let discardsize = player_data[0]["cardGroups"]["DISCARD"]["cardCount"];
        let removedsize = player_data[0]["cardGroups"]["REMOVED"]["cardCount"];
        let thescore = player_data[0]["score"];
        return {
            "username": username,
            "drawsize": drawsize,
            "handsize": handsize,
            "discardsize": discardsize,
            "removedsize": removedsize,
            "score": thescore
        };
    }
}

function get_your_player_data(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    return get_player_data(your_player_id, gamestate);
}

function get_opponent_player_data(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    let opponent_player_data = gamestate["players"].filter((data) => data["playerId"] != your_player_id);
    let opponent_player_id = opponent_player_data[0]["playerId"];
    return get_player_data(opponent_player_id, gamestate);
}

export default function PlayerScorePane ( {gamestate} ) {
    let badge_color = 'secondary';
    let your_player_data = get_your_player_data(gamestate);
    let opponent_player_data = get_opponent_player_data(gamestate);

    return(
        <Box id="PlayerScorePane">
            {/* YOUR ROW */}
            <Box id="yourPlayerName">
                <Typography>{your_player_data.username}</Typography>
            </Box>
            <Box>
                <Typography>YOUR CLOCK</Typography>
            </Box>
            <Box id="yourDrawDeckSize" sx={{backgroundImage: `url(${decipher_card_deck})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Draw deck size">
                    <Typography align='center'>{your_player_data.drawsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="yourHandSize" sx={{backgroundImage: `url(${decipher_card_hand})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Hand size">
                    <Typography align='center'>{your_player_data.handsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="yourDiscardSize" sx={{backgroundImage: `url(${decipher_card_discard})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Discard size">
                    <Typography align='center'>{your_player_data.discardsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="yourRemovedSize" sx={{backgroundImage: `url(${decipher_card_logo_only})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Removed from game">
                    <Typography align='center'>{your_player_data.removedsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="yourScore">
                <Typography align='center'>SCORE: {your_player_data.score}</Typography>
            </Box>

            {/* OPPONENT ROW */}
            <Box id="opponentPlayerName">
                <Typography>{opponent_player_data.username}</Typography>
            </Box>
            <Box>
                <Typography>THEIR CLOCK</Typography>
            </Box>
            <Box id="opponentDrawDeckSize" sx={{backgroundImage: `url(${decipher_card_deck})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Draw deck size">
                    <Typography align='center'>{opponent_player_data.drawsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="opponentHandSize" sx={{backgroundImage: `url(${decipher_card_hand})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Hand size">
                    <Typography align='center'>{opponent_player_data.handsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="opponentDiscardSize" sx={{backgroundImage: `url(${decipher_card_discard})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Discard size">
                    <Typography align='center'>{opponent_player_data.discardsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="opponentRemovedSize" sx={{backgroundImage: `url(${decipher_card_logo_only})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat'}}>
                <Tooltip title="Removed from game">
                    <Typography align='center'>{opponent_player_data.removedsize}</Typography>
                </Tooltip>
            </Box>
            <Box id="opponentScore">
                <Typography align='center'>SCORE: {opponent_player_data.score}</Typography>
            </Box>
        </Box>
    );
}