// A CardStack represents any card that could have items placed on it or under it.
// We will display up to three cards on top of it
// We will display up to three cards beneath it.
import Box from "@mui/material/Box";
import Popover from '@mui/material/Popover';
import Stack from "@mui/material/Stack";
import Typography from '@mui/material/Typography';
import Chip from "@mui/material/Chip";
import PersonIcon from '@mui/icons-material/Person';
import CheckIcon from '@mui/icons-material/Check';
import ClearIcon from '@mui/icons-material/Clear';
import { useState } from "react";
import Card from "./card.jsx";
import { theme } from '../../js/gemp-022/common.js';

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

function numPersonnelAttached(gamestate, anchor_id) {
    let beneath_arr = [];
    for (const [id, cardData] of Object.entries(gamestate["visibleCardsInGame"])) {
        if ((cardData.attachedToCardId != null) &&
            (cardData.attachedToCardId === anchor_id) &&
            (cardData.cardType === "PERSONNEL") &&
            (cardData.isInPlay)) {
                beneath_arr.push(cardData);
        }
    }

    return beneath_arr.length;
}

function cardInStackHasValidAction(gamestate, allCardsInStack) {
    if (Object.hasOwn(gamestate, "pendingDecision")) {
        if (gamestate.pendingDecision.context === "SELECT_PHASE_ACTION") {
            if (Object.hasOwn(gamestate.pendingDecision, "actions")) {
                // collect up cards with actions
                let possible_cardIds_with_actions = [];
                for (const possible_action of gamestate.pendingDecision.actions) {
                    if (possible_action.performingCardId) {
                        possible_cardIds_with_actions.push(possible_action.performingCardId);
                    }
                }

                // compare current stack cards against actionable cards
                for (const stackCardData of allCardsInStack) {
                    if (possible_cardIds_with_actions.includes(stackCardData.cardId)) {
                        return true;
                    }
                }
            }
        }
        // older code, may not be up to date with gamestate
        else if (Object.hasOwn(gamestate.pendingDecision, "cardIds")) {
            for (const stackCardData of allCardsInStack) {
                if (gamestate.pendingDecision.cardIds.includes(stackCardData.cardId.toString())) {
                    return true;
                }
            }
        }
    }

    return false;
}

function cardTooltipTitle(card, gamestate) {
    if (card.cardType === "PERSONNEL") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        let retstring = "";
        if (card.uniqueness === "UNIVERSAL") {
            const universalDiamond = `\u2756`.normalize();
            retstring = `${universalDiamond} ${card.title}`;
        }
        else {
            retstring = `${card.title}`;
        }
        return(retstring);
    }
    else if (card.cardType === "FACILITY") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        return(card.title);
    }
    else if (card.cardType === "SHIP") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        let retstring = "";
        if (card.uniqueness === "UNIVERSAL") {
            const universalDiamond = `\u2756`.normalize();
            retstring = `${universalDiamond} ${card.title}`;
        }
        else {
            retstring = `${card.title}`;
        }
        return(retstring);
    }
    else if (card.cardType === "MISSION") {
        let locationData;
        for (let spacelineLocation of Object.values(gamestate["spacelineLocations"])) {
            if (spacelineLocation.locationId === card.locationId) {
                locationData = spacelineLocation;
            }
        }

        let retstring = "";
        if (locationData) {
            if (locationData.region) {
                retstring = `${card.title} (${locationData.quadrant}, ${locationData.region})`;
            }
            else {
                retstring = `${card.title} (${locationData.quadrant})`;
            }
        }
        return(retstring);
    }
    else {
        // TODO: Do we want to show anything unique for these in the tooltip?
        // Artifact
        // Damage Marker
        // Dilemma
        // Doorway
        // Equipment
        // Event
        // Incident
        // Interrupt
        // Objective
        // Q Artifact
        // Q Dilemma
        // Q Event
        // Q Interrupt
        // Q Mission
        // Site
        // Tactic
        // Time Location
        // Tribble
        // Trouble
        return(card.title);
    }
}

export default function CardStack( {gamestate, anchor_id, openCardDetailsFunc, sx} ) {
    /* CardStack sets a grid with columns that are smaller than the content.
     * By setting a z-index and the minimum card width and height to values larger than the
     *   width and height of the cell, the card image overflows its grid cell
     *   giving the illusion of cards stacking on top of each other.
     */

    // Display Popover containing stack data on hover.
    // setup state
    const [onHoverAnchorElement, setOnHoverAnchorElement] = useState(null);
    // setup interactivity
    const onHoverOpen = (event) => {
        setOnHoverAnchorElement(event.currentTarget);
    };
    const onHoverClose = () => {
        setOnHoverAnchorElement(null);
    };
    // default closed
    const showLeftChips = false; // TODO
    const showRightChips = Boolean(onHoverAnchorElement);


    // Set minimum size of cards in the stack.
    const cardMinWidth = 65; //px
    const cardMinHeight = 90; //px
    const validActionBorder = 2; //px
    // BUG: This also sets the max width because cards aren't growing thanks to the grid trick.
    //      May have to set this dynamically based on document width? Grr. Gonna be weird.
    //      May have to go back to absolute positioned elements that are ~90% wide?

    // Get the card data
    let anchorCard = gamestate["visibleCardsInGame"][anchor_id.toString()];
    let cards_above = flat_create_card_objs_above(gamestate, anchor_id)
    let cards_beneath = flat_create_card_objs_beneath(gamestate, anchor_id);
    let allCards = cards_above.concat([anchorCard], cards_beneath);

    // anchor card data for chip display
    const isShip = (anchorCard.cardType === "SHIP");
    const isFacility = (anchorCard.cardType === "FACILITY");
    const isPlanet = (anchorCard.cardType === "MISSION");
    const isStaffed = (anchorCard.cardType === "SHIP"); // TODO: staffing check

    // Set minimum size of the stack as a whole.
    // Dependent on quantity of cards in the stack, calculated above.
    const nestedCardOffset = 5; //px
    const stackMinWidth = allCards.length > 1 ? `${cardMinWidth + (nestedCardOffset * allCards.length) + (validActionBorder * 2)}px` : `${cardMinWidth + (validActionBorder * 2)}px`;
    const stackMinHeight = allCards.length > 1 ? `${cardMinHeight + (nestedCardOffset * allCards.length) + (validActionBorder * 2)}px` : `${cardMinHeight + (validActionBorder * 2)}px`;

    const validAction = cardInStackHasValidAction(gamestate, allCards);
    const stackBorder = validAction ? `2px solid ${theme.palette.primary.light}` : "none";
    const stackBorderRadius =  validAction ? "7px" : "none";

    // Render the card data
    let reactCardObjs = allCards.map((cardData, i) => 
        <Card
            key={cardData.cardId}
            gamestate={gamestate}
            card={cardData}
            index={i}
            openCardDetailsFunc={openCardDetailsFunc}
            sx={{
                minWidth: `${cardMinWidth}px`,
                minHeight:`${cardMinHeight}px`
            }}
        />
    );

    return(
        <Box>
            <Box
                aria-owns={open ? `${anchor_id}-right-popover, ${anchor_id}-left-popover` : undefined}
                aria-haspopup="true"
                onMouseEnter={onHoverOpen}
                onMouseLeave={onHoverClose}
                data-cardstackanchorid={anchor_id}
                sx={{
                    minWidth: stackMinWidth,
                    minHeight: stackMinHeight,
                    display: "grid",
                    gridTemplateColumns: `repeat(${allCards.length}, ${nestedCardOffset}px)`,
                    gridTemplateRows: `repeat(${allCards.length}, ${nestedCardOffset}px)`,
                    border: stackBorder,
                    borderRadius: stackBorderRadius,
                    ...sx //also use incoming styles from parent
                }}>
                {reactCardObjs}
            </Box>
            <Popover
                id={`${anchor_id}-right-popover`}
                sx={{ pointerEvents: 'none' }}
                open={showRightChips}
                anchorEl={onHoverAnchorElement}
                anchorOrigin={{
                    vertical: 'center',
                    horizontal: 'right',
                }}
                transformOrigin={{
                    vertical: 'center',
                    horizontal: 'left',
                }}
                onClose={onHoverClose}
                disableRestoreFocus
            >
                <Stack direction="column">
                    <Typography align="center">{cardTooltipTitle(anchorCard, gamestate)}</Typography>
                    {isStaffed ? <Chip icon={<CheckIcon />} label="Staffed" variant="outlined" /> : null}
                    {(isShip || isFacility || isPlanet) ? <Chip icon={<PersonIcon />} label={`${numPersonnelAttached(gamestate, anchor_id)} Personnel`} variant="outlined" /> : null}
                </Stack>
            </Popover>
            <Popover
                id={`${anchor_id}-left-popover`}
                sx={{ pointerEvents: 'none' }}
                open={showLeftChips}
                anchorEl={onHoverAnchorElement}
                anchorOrigin={{
                    vertical: 'center',
                    horizontal: 'left',
                }}
                transformOrigin={{
                    vertical: 'center',
                    horizontal: 'right',
                }}
                onClose={onHoverClose}
                disableRestoreFocus
            >
                {/* TODO: Decide if I'm going to split these L/R or do one big stack */}
            </Popover>
        </Box>
    );
}