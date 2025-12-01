// A CardStack represents any card that could have items placed on it or under it.
// We will display up to three cards on top of it
// We will display up to three cards beneath it.
import { Stack } from "@mui/material";
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
        retarr.push(<Card key={cardData.cardId} card={cardData} />); 
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
        retarr.push(<Card key={cardData.cardId} card={cardData} />); 
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

    let max_three_cards = 3;
    let retarr = [];
    for (let i = 0; i < max_three_cards; i++) {
        let cardData = above_arr[i];
        if (cardData) {
            retarr.push(<Card key={cardData.cardId} card={cardData} />); 
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

    let max_three_cards = 3;
    let retarr = [];
    for (let i = 0; i < max_three_cards; i++) {
        let cardData = beneath_arr[i];
        if (cardData) {
            retarr.push(<Card key={cardData.cardId} card={cardData} />); 
        }
    }
    return retarr;
}

export default function CardStack( {gamestate, anchor_id} ) {
    let anchorCard = gamestate["visibleCardsInGame"][anchor_id.toString()];
    let cards_above = flat_create_card_objs_above(gamestate, anchor_id)
    let cards_beneath = flat_create_card_objs_beneath(gamestate, anchor_id);

    return(
        <Stack
            direction="column"
            spacing={2}
            sx={{
                justifyContent: "space-between",
                alignItems: "center",
                flexGrow: 1
            }}
        >
            {cards_above}
            <Card key={anchor_id} card={anchorCard} />
            {cards_beneath}
        </Stack>
    );
}