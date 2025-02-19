import Grid from '@mui/material/Grid2';
import { Typography } from '@mui/material';
import Tooltip from '@mui/material';
import decipher_card_deck from '../../images/decipher_card_deck.svg';
import decipher_card_discard from '../../images/decipher_card_discard.svg';
import decipher_card_hand from '../../images/decipher_card_hand.svg';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg';

export default function PlayerScorePane ( {gamestate} ) {
    return(
        <Grid container spacing={1} columns={10}>
            <Grid size={4}>
                <Typography>andrewd18</Typography>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Draw deck size">
                    <img src={decipher_card_deck} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Hand size">
                    <img src={decipher_card_hand} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Discard size">
                    <img src={decipher_card_discard} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Removed from game">
                    <img src={decipher_card_logo_only} />
                </Tooltip>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: 10</Typography>
            </Grid>
            <Grid size={4}>
                <Typography>DataNoh</Typography>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Draw deck size">
                    <img src={decipher_card_deck} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Hand size">
                    <img src={decipher_card_hand} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Discard size">
                    <img src={decipher_card_discard} />
                </Tooltip>
            </Grid>
            <Grid size={1}>
                <Tooltip title="Removed from game">
                    <img src={decipher_card_logo_only} />
                </Tooltip>
            </Grid>
            <Grid size={2}>
                <Typography>SCORE: 35</Typography>
            </Grid>
        </Grid>
    );
}