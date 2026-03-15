import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';
import Button from '@mui/material/Button';
import decipher_card_deck from '../../images/decipher_card_deck.svg?no-inline';
import decipher_card_discard from '../../images/decipher_card_discard.svg?no-inline';
import decipher_card_hand from '../../images/decipher_card_hand.svg?no-inline';
import decipher_card_removed from '../../images/decipher_card_removed.svg?no-inline';
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import WorkspacePremiumIcon from '@mui/icons-material/WorkspacePremium';
import '../../css/player-score-pane.css';

function get_player_data(player_id, gamestate) {
    let failure_case = {
        "isCurrentPlayer": false,
        "isFirstPlayer": false,
        "username": "",
        "drawsize": 0,
        "handsize": 0,
        "discardsize": 0,
        "removedsize": 0,
        "totalPoints": 0,
        "missionPoints": 0,
        "bonusPoints": 0,
        "clock": "0:00"
    };

    if (Object.hasOwn(gamestate, "playerMap")) {
        let player_data = gamestate.playerMap[player_id];
        if (player_data != null) {
            let isCurrentPlayer = gamestate["playerOrder"]["currentPlayer"] == player_data["playerId"] ? true : false;
            let isFirstPlayer = gamestate["playerOrder"]["firstPlayer"] == player_data["playerId"] ? true : false;
            let username = player_data["playerId"];
            let drawsize = player_data["cardGroups"]["DRAW_DECK"]["cardCount"];
            let handsize = player_data["cardGroups"]["HAND"]["cardCount"];
            let discardsize = player_data["cardGroups"]["DISCARD"]["cardCount"];
            let removedsize = player_data["cardGroups"]["REMOVED"]["cardCount"];
            let totalPoints = player_data["points"]["total"];
            let missionPoints = player_data["points"]["nonBonus"];
            let bonusPoints = player_data["points"]["bonus"];

            let player_clock;
            for (const clockObj of gamestate["playerClocks"]) {
                if (clockObj.playerId === player_id) {
                    let date = new Date(0);
                    date.setSeconds(parseInt(clockObj.timeRemaining));
                    player_clock = date.toISOString().substring(11, 19);
                }
            }
            
            return {
                "isCurrentPlayer": isCurrentPlayer,
                "isFirstPlayer": isFirstPlayer,
                "username": username,
                "clock": player_clock,
                "drawsize": drawsize,
                "handsize": handsize,
                "discardsize": discardsize,
                "removedsize": removedsize,
                "totalPoints": totalPoints,
                "missionPoints": missionPoints,
                "bonusPoints": bonusPoints,
            };
        }
        // Playermap but no ID
        else {
            console.error(`player with id ${player_id} not found`);
            // 0 here hides the number badges
            return failure_case;
        }
    }
    else {
        console.error(`gamestate.playerMap not found`);
        console.error(gamestate);
        // 0 here hides the number badges
        return failure_case;    
    }
}

function missionAndBonusPointsTooltip(player_data) {
    return(
        <Stack direction="column">
            <Typography>Mission points: {player_data.missionPoints}</Typography>
            <Typography>Bonus points: {player_data.bonusPoints}</Typography>
        </Stack>
    )
}

export default function PlayerScorePane ( {gamestate, player_id, openPileDetailsFunc} ) {
    let player_data = get_player_data(player_id, gamestate);

    return(
        <Box className="PlayerScorePane">
            <Stack direction="row" spacing={0.5} className="PlayerName">
                {player_data.isCurrentPlayer ? <Tooltip title="Current player"><KeyboardDoubleArrowRightIcon /></Tooltip> : null}
                <Typography>{player_data.username}</Typography>
                {player_data.isFirstPlayer ? <Tooltip title="First player"><WorkspacePremiumIcon/></Tooltip> : null}
            </Stack>
            <Box>
                <Tooltip title="Remaining clock">
                    <Typography>{player_data.clock}</Typography>
                </Tooltip>
            </Box>
            <Box className="DrawDeckSize" sx={{minWidth: '42px', backgroundImage: `url(${decipher_card_deck})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Draw deck size">
                    <Typography align='center'>{player_data.drawsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="HandSize" sx={{minWidth: '42px', backgroundImage: `url(${decipher_card_hand})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Hand size">
                    <Typography align='center'>{player_data.handsize}</Typography>
                </Tooltip>
            </Box>
            <Box className="DiscardSize" sx={{minWidth: '42px', backgroundImage: `url(${decipher_card_discard})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Discard size">
                    <Button color="white" sx={{padding: 0, minWidth: '42px', width: "100%"}} onClick={() => openPileDetailsFunc({playerId: player_id, pileName: "DISCARD"})}>
                        <Typography align='center'>{player_data.discardsize}</Typography>
                    </Button>
                </Tooltip>
            </Box>
            <Box className="RemovedSize" sx={{minWidth: '42px', backgroundImage: `url(${decipher_card_removed})`, backgroundSize: '42px 42px', backgroundRepeat: 'no-repeat', backgroundPosition: 'center'}}>
                <Tooltip title="Removed from game">
                    <Button color="white" sx={{padding: 0, minWidth: '42px', width: "100%"}} onClick={() => openPileDetailsFunc({playerId: player_id, pileName: "REMOVED"})}>
                        <Typography align='center'>{player_data.removedsize}</Typography>
                    </Button>
                </Tooltip>
            </Box>
            <Box className="Score">
                <Tooltip title={missionAndBonusPointsTooltip(player_data)}>
                    <Button color="white" sx={{padding: 0, minWidth: 0}} onClick={() => openPileDetailsFunc({playerId: player_id, pileName: "POINT_AREA"})}>
                        <Typography align='center'>
                            SCORE: {player_data.totalPoints}
                        </Typography>
                    </Button>
                </Tooltip>
            </Box>
            
        </Box>
    );
}