import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Badge from '@mui/material/Badge';
import CircularProgress from '@mui/material/CircularProgress';
import Tooltip from '@mui/material/Tooltip';
import Button from '@mui/material/Button';
import DangerousIcon from '@mui/icons-material/Dangerous';
import { useTrekccImage } from '../hooks/useTrekccImage.jsx';

/*
example card data: {
  "cardId": 55,
  "title": "Jadzia Dax",
  "blueprintId": "112_208",
  "owner": "andrew",
  "locationId": 7,
  "attachedToCardId": 48,
  "isStopped": false,
  "imageUrl": "https://www.trekcc.org/1e/cardimages/ds9/jadziadax.gif",
  "cardType": "PERSONNEL",
  "uniqueness": "UNIQUE"
}
*/

const HideAfterTimeout = ({children, delayMs}) => {
    let timer = null;

    const [visible, setVisible] = useState(true);

    function setTimer() {
        if (timer) {
            clearTimeout(timer);
        }
        else {
            timer = setTimeout(() => {
                setVisible(false);
                timer = null;
            }, delayMs);
        }
    }
    setTimer(delayMs);
    let retval = visible ? children : null;
    return retval;
} 

export default function Card( {card, index, openCardDetailsFunc, sx} ) {
    const imageUrl = useTrekccImage(card.imageUrl);

    const columnPosition = index ? `${index+1}/auto` : undefined;
    const rowPosition = index ? `${index+1}/auto` : undefined;
    const cardZIndex = index ? -index : 0;

    return(
        <Box
            data-cardid={card.cardId}
            sx={{
                gridColumn: columnPosition,
                gridRow: rowPosition,
                zIndex: cardZIndex,
                ...sx //also use incoming styles from parent
            }}
        >
            <Button onClick={() => openCardDetailsFunc(card.cardId)} sx={{height: "100%", width:"100%", padding: "0px"}}>
                {/* If imageurl is null, show a circular progress spinner, otherwise load the graphic. */}
                {imageUrl ? <img height={"100%"} width={"100%"} src={imageUrl} /> : <HideAfterTimeout delayMs={3000} children={<CircularProgress />} />}
            </Button>
        </Box>
    );
}