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

export default function Card( {card, gamestate, index, openCardDetailsFunc, sx} ) {
    let badge_color = "error";
    let stopped_badge = 0; // hidden by default
    let overlay = {};
    if (card.isStopped) {
        stopped_badge = "Stopped";
        overlay = {filter: "grayscale(80%)"};
    }
    
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
            {/*<Badge color={badge_color} badgeContent={stopped_badge}>*/}
            <Button onClick={() => openCardDetailsFunc(card.cardId)} sx={{height: "100%", width:"100%", padding: "0px"}}>
                {/* If imageurl is null, show a circular progress spinner, otherwise load the graphic. */}
                {imageUrl ? <img height={"100%"} width={"100%"} src={imageUrl} style={overlay} /> : <CircularProgress/>}
            </Button>
            {/*</Badge>*/}
        </Box>
    );
}