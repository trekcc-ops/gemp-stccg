// A CardStack represents any card that could have items placed on it or under it.
// We will display up to three cards on top of it
// We will display up to three cards beneath it.
import { Box } from "@mui/material";
import Card from "./card.jsx";

// TODO: This is dependent on gamestate having an attached cardTreeModel.
//       Use flat version until such time as that's integrated.
//
//       Also dependent on a placedOnTop array being created in the cardTreeModel.
function tree_create_card_objs_above(anchor_card) {
    let above_arr = [];
    let max_three_cards = 3;
    for (let i = 0; i < max_three_cards; i++) {
        if (anchor_card.placedOnTop) {
            above_arr.push(anchor_card.placedOnTop[i]);
        }
    }

    let retarr = [];
    for (const cardData of above_arr) {
        retarr.push(cardData); 
    }
    return retarr;
}

// TODO: This is dependent on gamestate having an attached cardTreeModel.
//       Use flat version until such time as that's integrated.
function tree_create_card_objs_beneath(anchor_card) {
    let beneath_arr = [];
    let max_three_cards = 3;
    for (let i = 0; i < max_three_cards; i++) {
        if (anchor_card.children) {
            beneath_arr.push(anchor_card.children[i]);
        }
    }

    let retarr = [];
    for (const cardData of beneath_arr) {
        retarr.push(cardData); 
    }
    return retarr;
}

function flat_create_card_objs_above(gamestate, anchor_id) {
    let above_arr = [];

    for (const [id, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if ((cardData.isPlacedOnMission != null) &&
            (cardData.isPlacedOnMission === anchor_id) &&
            (cardData.isInPlay)) {
                above_arr.push(cardData);
        }
    }

    let max_three_cards = 4; // 1 indexed since z-index 0 is the anchor card
    let retarr = [];
    for (let i = 1; i < max_three_cards; i++) {
        let cardData = above_arr[i];
        if (cardData) {
            retarr.push(cardData);
        }
    }
    return retarr;
}

function flat_create_card_objs_beneath(gamestate, anchor_id) {
    let beneath_arr = [];

    for (const [id, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if ((cardData.attachedToCardId != null) &&
            (cardData.attachedToCardId === anchor_id) &&
            (cardData.isInPlay)) {
                beneath_arr.push(cardData);
        }
    }

    let max_three_cards = 3; // 1 indexed since z-index 0 is the anchor card
    let retarr = [];
    for (let i = 0; i < max_three_cards; i++) {
        let cardData = beneath_arr[i];
        if (cardData) {
            retarr.push(cardData);
        }
    }
    return retarr;
}

export default function CardStack( {gamestate, anchor_id, sx} ) {
    /* CardStack sets a grid with columns that are smaller than the content.
     * By setting a z-index and the minimum card width and height to values larger than the
     *   width and height of the cell, the card image overflows its grid cell
     *   giving the illusion of cards stacking on top of each other.
     */


    // Set minimum size of cards in the stack.
    const cardMinWidth = 65; //px
    const cardMinHeight = 90; //px
    // BUG: This also sets the max width because cards aren't growing thanks to the grid trick.
    //      May have to set this dynamically based on document width? Grr. Gonna be weird.
    //      May have to go back to absolute positioned elements that are ~90% wide?

    // Get the card data
    let anchorCard = gamestate["visibleCardsInGame"][anchor_id.toString()];
    let cards_above = flat_create_card_objs_above(gamestate, anchor_id)
    let cards_beneath = flat_create_card_objs_beneath(gamestate, anchor_id);
    let allCards = cards_above.concat([anchorCard], cards_beneath);

    // Set minimum size of the stack as a whole.
    // Dependent on quantity of cards in the stack, calculated above.
    const nestedCardOffset = 5; //px
    const stackMinWidth = allCards.length > 1 ? `${cardMinWidth + (nestedCardOffset * allCards.length)}px` : `${cardMinWidth}px`;
    const stackMinHeight = allCards.length > 1 ? `${cardMinHeight + (nestedCardOffset * allCards.length)}px` : `${cardMinHeight}px`;

    // Render the card data
    let reactCardObjs = allCards.map((cardData, i) => 
        <Card
            key={cardData.cardId}
            gamestate={gamestate}
            card={cardData}
            index={i}
            sx={{
                minWidth: `${cardMinWidth}px`,
                minHeight:`${cardMinHeight}px`
            }}
        />
    );

    return(
        <Box
            data-cardstackanchorid={anchor_id}
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