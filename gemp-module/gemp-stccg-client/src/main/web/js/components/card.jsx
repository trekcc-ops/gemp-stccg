import { useState, useEffect } from 'react';
import { Box, Badge, CircularProgress } from '@mui/material';
import DangerousIcon from '@mui/icons-material/Dangerous';
import { fetchImage } from '../gemp-022/communication.js';

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

export default function Card( {card} ) {
    let badge_color = "error";
    let stopped_badge = 0; // hidden by default
    let overlay = {};
    if (card.isStopped) {
        stopped_badge = "Stopped";
        overlay = {filter: "grayscale(80%)"};
    }

    const [imageUrl, setImageUrl] = useState(null);

    useEffect(() => {
        fetchImage(card.imageUrl)
        .then((url) => {
            setImageUrl(url);
        });
    },[]);

    return(
        <Box data-cardid={card.cardId} sx={{height: 1, width: 1}} >
            <Badge color={badge_color} badgeContent={stopped_badge}>
                {/* If imageurl is null, show a circular progress spinner, otherwise load the graphic. */}
                {imageUrl ? <img width={"100%"} src={imageUrl} style={overlay} /> : <CircularProgress/>}
            </Badge>
        </Box>
    );
}