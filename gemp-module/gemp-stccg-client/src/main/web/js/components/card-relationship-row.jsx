import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Grid from "@mui/material/Grid";
import CardStack from "./card-stack";

function find_cards_by_filter(gamestate, cardData, cardPropertyFilter) {
    let matches = [];
    for (const [id, card] of Object.entries(gamestate["visibleCardsInGame"])) {
        if (Object.hasOwn(card, cardPropertyFilter)) {
            if (card[cardPropertyFilter] === cardData.cardId) {
                matches.push(card);
            }
        }
    }
    return matches;
}


export default function CardRelationshipRow({title, gamestate, cardData, cardPropertyFilter, openCardDetailsFunc}) {
    let children = find_cards_by_filter(gamestate, cardData, cardPropertyFilter);
    let cardStacks = children.map((card) => 
        <Grid>
            <CardStack key={card.cardId} gamestate={gamestate} anchor_id={card.cardId} openCardDetailsFunc={openCardDetailsFunc} />
        </Grid>
    );
    return(
        <Box>
            {/* If we have children, then show the row. */}
            {children.length > 0 &&
                <Stack direction={"column"} spacing={2}>
                    <Typography>{title}</Typography>
                    <Grid container spacing={2} >
                        {cardStacks}
                    </Grid>
                </Stack>
            }
        </Box>
    )
} 