import { useState, useEffect } from 'react';
import { Box, Badge, CircularProgress } from '@mui/material';
import DangerousIcon from '@mui/icons-material/Dangerous';
import { fetchImage } from '../gemp-022/communication.js';
import { useTrekccImage } from '../hooks/useTrekccImage.jsx';

/*
example card: {
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

export default function Card( {card, index, sx} ) {
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
                {/* If imageurl is null, show a circular progress spinner, otherwise load the graphic. */}
                {imageUrl ? <img height={"100%"} width={"100%"} src={imageUrl} style={overlay} /> : <CircularProgress/>}
            {/*</Badge>*/}
        </Box>
    );
}