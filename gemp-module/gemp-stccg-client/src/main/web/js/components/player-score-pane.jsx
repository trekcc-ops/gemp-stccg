import Grid from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';
import decipher_card_deck from '../../images/decipher_card_deck.svg';
import decipher_card_discard from '../../images/decipher_card_discard.svg';
import decipher_card_hand from '../../images/decipher_card_hand.svg';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg';

// TODO: Probably going to have to rework this to use divs and the CSS grid from the css file,
//       since this isn't scaling as expected with the auto React grid.

export default function PlayerScorePane ( {gamestate} ) {
    return(
        <Grid container spacing={1} columns={8}>
            <Grid size={2}>
                <Typography>andrewd18</Typography>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Draw deck size">
                    <img src={decipher_card_deck} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Hand size">
                    <img src={decipher_card_hand} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Discard size">
                    <img src={decipher_card_discard} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Removed from game">
                    <img src={decipher_card_logo_only} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: 10</Typography>
            </Grid>
            <Grid size={2}>
                <Typography>DataNoh</Typography>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Draw deck size">
                    <img src={decipher_card_deck} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Hand size">
                    <img src={decipher_card_hand} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Discard size">
                    <img src={decipher_card_discard} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Removed from game">
                    <img src={decipher_card_logo_only} style={{height: 'inherit', width: 'inherit', minHeight: '30px', minWidth: '20px'}} />
                </Tooltip>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: 35</Typography>
            </Grid>
        </Grid>
    );
}