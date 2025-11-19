import Box from '@mui/material/Box';
import { Badge } from '@mui/material';
import DangerousIcon from '@mui/icons-material/Dangerous';

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

    // TODO: Instead of raw url, use comms.fetchImage() after PR 225 merges.

    return(
        <Box data-cardid={card.cardId} sx={{height: 1, width: 1}} >
            <Badge color={badge_color} badgeContent={stopped_badge}>
                <img width={"100%"} src={card.imageUrl} style={overlay} />
            </Badge>
        </Box>
    );
}