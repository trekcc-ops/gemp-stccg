import Grid from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import Badge from '@mui/material/Badge';
import Tooltip from '@mui/material/Tooltip';
import decipher_card_deck from '../../images/decipher_card_deck.svg';
import decipher_card_discard from '../../images/decipher_card_discard.svg';
import decipher_card_hand from '../../images/decipher_card_hand.svg';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg';

// TODO: Probably going to have to rework this to use divs and the CSS grid from the css file,
//       since this isn't scaling as expected with the auto React grid.

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
    return get_player_data(opponent_player_data, gamestate);
}

export default function PlayerScorePane ( {gamestate} ) {
    let badge_color = 'secondary';
    let your_player_data = get_your_player_data(gamestate);
    let opponent_player_data = get_opponent_player_data(gamestate);

    return(
        <Grid container spacing={1} columns={8}>
            <Grid size={2}>
                <Typography>{your_player_data.username}</Typography>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={your_player_data.drawsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Draw deck size">
                        <img src={decipher_card_deck} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={your_player_data.handsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Hand size">
                        <img src={decipher_card_hand} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={your_player_data.discardsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Discard size">
                        <img src={decipher_card_discard} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={your_player_data.removedsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Removed from game">
                        <img src={decipher_card_logo_only} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: {your_player_data.score}</Typography>
            </Grid>
            <Grid size={2}>
                <Typography>{opponent_player_data.username}</Typography>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={opponent_player_data.drawsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Draw deck size">
                        <img src={decipher_card_deck} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={opponent_player_data.handsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Hand size">
                        <img src={decipher_card_hand} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={opponent_player_data.discardsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Discard size">
                        <img src={decipher_card_discard} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={1}>
                <Badge color={badge_color} badgeContent={opponent_player_data.removedsize} showZero anchorOrigin={{vertical: 'top', horizontal: 'left',}}>
                    <Tooltip title="Removed from game">
                        <img src={decipher_card_logo_only} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                    </Tooltip>
                </Badge>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: {opponent_player_data.score}</Typography>
            </Grid>
        </Grid>
    );
}