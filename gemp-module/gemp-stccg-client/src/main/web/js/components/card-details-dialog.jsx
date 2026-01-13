import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Card from './card';
import Stack from '@mui/material/Stack';
import NorthIcon from '@mui/icons-material/North';
import Typography from '@mui/material/Typography';
import CardRelationshipRow from './card-relationship-row';
import ButtonGroup from '@mui/material/ButtonGroup';
import SyncAltIcon from '@mui/icons-material/SyncAlt';
import DataArrayIcon from '@mui/icons-material/DataArray';
import DeveloperModeIcon from '@mui/icons-material/DeveloperMode';
import ViewWeekIcon from '@mui/icons-material/ViewWeek';

function flyButton(gamestate, cardData) {
    if (cardData.cardType != "SHIP") {
        return;
    }

    let your_player_id = gamestate["requestingPlayer"];

    // Reasons to show the icon but disable it
    let isDisabled = false;
    if (cardData.owner != your_player_id) {
        isDisabled = true;
    }

    let flyActionThisCard = false;
    if (Object.hasOwn(gamestate, "pendingDecision")) {
        if (Object.hasOwn(gamestate.pendingDecision, "cardIds")) {
            // if this card is listed in the pending decision ids
            if (gamestate.pendingDecision.cardIds.includes(cardData.cardId.toString())) {
                // Check if there are any actions for this card that are beaming
                for (const decision of gamestate.pendingDecision.displayedCards) {
                    if (decision.actionType === "FLY_SHIP" &&
                        decision.cardId === cardData.cardId.toString()) {
                            flyActionThisCard = true;
                    }
                }
            }
        }
    }

    // always show Fly button
    if (!flyActionThisCard || isDisabled) {
        return(<Button disabled startIcon={<SyncAltIcon/>}>Fly</Button>);
    }
    else {
        return(<Button startIcon={<SyncAltIcon/>}>Fly</Button>);
    }
}

function dockUndockButton(gamestate, cardData) {
    if (cardData.cardType != "SHIP") {
        return;
    }

    let your_player_id = gamestate["requestingPlayer"];

    // Reasons to show the icon but disable it
    let isDisabled = false;
    if (cardData.owner != your_player_id) {
        isDisabled = true;
    }
    
    let dockActionThisCard = false;
    let undockActionThisCard = false;
    if (Object.hasOwn(gamestate, "pendingDecision")) {
        if (Object.hasOwn(gamestate.pendingDecision, "cardIds")) {
            // if this card is listed in the pending decision ids
            if (gamestate.pendingDecision.cardIds.includes(cardData.cardId.toString())) {
                // Check if there are any actions for this card that are beaming
                for (const decision of gamestate.pendingDecision.displayedCards) {
                    if (decision.actionType === "DOCK_SHIP" &&
                        decision.cardId === cardData.cardId.toString()) {
                            dockActionThisCard = true;
                    }

                    if (decision.actionType === "UNDOCK_SHIP" &&
                        decision.cardId === cardData.cardId.toString()) {
                            undockActionThisCard = true;
                    }
                }
            }
        }
    }

    // Only show dock or undock button if we have a relevant action
    if (dockActionThisCard) {
        if (isDisabled) {
            return(<Button disabled startIcon={<DataArrayIcon/>}>Dock</Button>);
        }
        else {
            return(<Button startIcon={<DataArrayIcon/>}>Dock</Button>);
        }
    }
    else if (undockActionThisCard) {
        if (isDisabled) {
            return(<Button disabled startIcon={<DeveloperModeIcon/>}>Undock</Button>);
        }
        else {
            return(<Button startIcon={<DeveloperModeIcon/>}>Undock</Button>);
        }
    }
    else {
        return;
    }
}

function beamButton(gamestate, cardData) {
    if (cardData.cardType != "SHIP" &&
        cardData.cardType != "FACILITY") {
        return;
    }

    let your_player_id = gamestate["requestingPlayer"];

    // Reasons to show the icon but disable it
    let isDisabled = false;
    if (cardData.owner != your_player_id) {
        isDisabled = true;
    }

    let beamActionThisCard = false;
    if (Object.hasOwn(gamestate, "pendingDecision")) {
        if (Object.hasOwn(gamestate.pendingDecision, "cardIds")) {
            // if this card is listed in the pending decision ids
            if (gamestate.pendingDecision.cardIds.includes(cardData.cardId.toString())) {
                // Check if there are any actions for this card that are beaming
                for (const decision of gamestate.pendingDecision.displayedCards) {
                    if (decision.actionType === "BEAM_CARDS" &&
                        decision.cardId === cardData.cardId.toString()) {
                            beamActionThisCard = true;
                    }
                }
            }
        }
    }

    if (beamActionThisCard) {
        if (isDisabled) {
            return(<Button disabled startIcon={<ViewWeekIcon/>}>Beam</Button>);
        }
        else {
            return(<Button startIcon={<ViewWeekIcon/>}>Beam</Button>);
        }
    }
    else {
        return;
    }
}

function attemptButton(gamestate, cardData) {

}

function battleButton(gamestate, cardData) {

}

function tractorButton(gamestate, cardData) {

}

function changeAffiliationButton(gamestate, cardData) {

}

function specialDownloadButton(gamestate, cardData) {

}

function bookmarkButton(gamestate, cardData) {

}


export default function CardDetailsDialog( {gamestate, cardId, setCardIdFunc, isOpen, onCloseFunc} ) {
    let cardData = cardId ? gamestate["visibleCardsInGame"][cardId] : undefined;
    if (cardData){
        let cardTitle = cardData.title;
        return(
            <Dialog open={isOpen} onClose={onCloseFunc}>
                <DialogTitle>Card Details: {cardTitle}</DialogTitle>
                <Stack direction={"row"} spacing={2}>
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        {/* Only display the UP button if the card is attached to something. */}
                        {Object.hasOwn(cardData, "attachedToCardId") &&
                            <Button variant='contained' onClick={() => setCardIdFunc(cardData["attachedToCardId"])}>
                                <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                                    <NorthIcon />
                                    <Typography>UP</Typography>
                                </Stack>
                            </Button>
                        }
                    </Stack>
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        <Card card={cardData} gamestate={gamestate} openCardDetailsFunc={setCardIdFunc} />
                        <Box>
                            <ButtonGroup variant="contained" aria-label="Card actions">
                                {flyButton(gamestate, cardData)}
                                {dockUndockButton(gamestate, cardData)}
                                {beamButton(gamestate, cardData)}
                            </ButtonGroup>
                        </Box>
                        <Box><Typography>Card Details</Typography></Box>
                    </Stack>
                    
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        <CardRelationshipRow title={"Attached"} gamestate={gamestate} cardData={cardData} cardPropertyFilter={"attachedToCardId"} openCardDetailsFunc={setCardIdFunc} />
                        {/* TODO: Add relationships like on top, modified by, etc. */}
                    </Stack>
                </Stack>
            </Dialog>
        )
    }
    else {
        return(
            <Dialog open={isOpen} onClose={onCloseFunc}>
                <DialogTitle>Card Details: N/A</DialogTitle>
            </Dialog>
        )
    }
    
}