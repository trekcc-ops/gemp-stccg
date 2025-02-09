import Box from '@mui/material/Box';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg';

export default function ActiveCardPane( {card} ) {
    // TODO: add action map and action buttons
    let card_src;
    if (!card) {
        card_src = decipher_card_logo_only;
    }
    else {
        card_src = card.imageUrl;
    }

    return(
        <Box>
            <img src={card_src} />
        </Box>
    );
}