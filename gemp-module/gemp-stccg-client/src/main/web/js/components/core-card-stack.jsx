// A CardStack represents any card that could have items placed on it or under it.
// We will display up to three cards on top of it
// We will display up to three cards beneath it.
import { Box } from "@mui/material";
import Card from "./card.jsx";
import decipher_card_core from '../../images/decipher_card_core.svg?url';

function get_core_cards(gamestate, playerid) {
    let retarr = [];
    let ids_in_core = [];

    let playerData = gamestate.playerMap[playerid];
    for (const cardid of playerData["cardGroups"]["CORE"]["cardIds"]) {
        ids_in_core.push(cardid.toString());
    }

    let visible_cards_in_game = gamestate["visibleCardsInGame"];
    for (const cardid of ids_in_core) {
        if (Object.hasOwn(visible_cards_in_game, cardid.toString())) {
            const cardData = visible_cards_in_game[cardid.toString()];
            if (cardData.owner === playerid &&
                cardData.isInPlay === true) {
                
                retarr.push(cardData);
            }
        }
    }

    return retarr;
}

// TODO: Decide if we need this of if we're going to keep the separate Core row.
export default function CoreCardStack( {gamestate, player_id, sx} ) {
    /* CardStack sets a grid with columns that are smaller than the content.
     * By setting a z-index and the minimum card width and height to values larger than the
     *   width and height of the cell, the card image overflows its grid cell
     *   giving the illusion of cards stacking on top of each other.
     */


    // Set minimum size of cards in the stack.
    const cardMinWidth = 80; //px
    const cardMinHeight = 110; //px
    // BUG: This also sets the max width because cards aren't growing thanks to the grid trick.
    //      May have to set this dynamically based on document width? Grr. Gonna be weird.
    //      May have to go back to absolute positioned elements that are ~90% wide?

    // Get the card data
    let anchorCard = [{
            "cardId": `core-${player_id}`,
            "title": "Core",
            "owner": `${player_id}`,
            "imageUrl": decipher_card_core,
        }];
    let cards_beneath = get_core_cards(gamestate, player_id);
    let allCards = anchorCard.concat(cards_beneath);

    // Set minimum size of the stack as a whole.
    // Dependent on quantity of cards in the stack, calculated above.
    const nestedCardOffset = 10; //px
    const stackMinWidth = allCards.length > 1 ? `${cardMinWidth + (nestedCardOffset * allCards.length)}px` : `${cardMinWidth}px`;
    const stackMinHeight = allCards.length > 1 ? `${cardMinHeight + (nestedCardOffset * allCards.length)}px` : `${cardMinHeight}px`;

    // Render the card data
    let reactCardObjs = allCards.map((cardData, i) => 
        <Card
            key={cardData.cardId}
            card={cardData}
            index={i}
            inc_minWidth={`${cardMinWidth}px`}
            inc_minHeight={`${cardMinHeight}px`}
        />
    );

    return(
        <Box
            data-cardstackanchorid={`core-${player_id}`}
            flexGrow={1}
            sx={{
                minWidth: stackMinWidth,
                minHeight: stackMinHeight,
                display: "grid",
                gridTemplateColumns: `repeat(${allCards.length}, ${nestedCardOffset}px)`,
                gridTemplateRows: `repeat(${allCards.length}, ${nestedCardOffset}px)`,
                ...sx //also use incoming styles from parent
            }}>
            {reactCardObjs}
        </Box>
    );
}