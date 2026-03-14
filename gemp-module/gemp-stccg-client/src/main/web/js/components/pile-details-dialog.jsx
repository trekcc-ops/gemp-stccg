import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import Stack from '@mui/material/Stack';
import CardStack from './card-stack';
import Grid from '@mui/material/Grid';

function pileNameToFriendlyName(pileName) {
    switch(pileName) {
        case "CORE":
            return "Core";
        case "MISSIONS_PILE":
            return "Missions deck";
        case "POINT_AREA":
            return "Point area";
        case "DISCARD":
            return "Discard";
        case "DRAW_DECK":
            return "Draw deck";
        case "HAND":
            return "Hand";
        case "SEED_DECK":
            return "Seed deck";
        case "REMOVED":
            return "Removed from game";
        default:
            return pileName;
    }
}


export default function PileDetailsDialog( {gamestate, pileDetailsPlayerIdAndPileName, isOpen, onCloseFunc, openCardDetailsFunc} ) {
    if (pileDetailsPlayerIdAndPileName) {
        let playerId = pileDetailsPlayerIdAndPileName.playerId;
        let pileName = pileDetailsPlayerIdAndPileName.pileName;
        let cardIdArray = gamestate["playerMap"][playerId]["cardGroups"][pileName]["cardIds"];
        let idsToView = cardIdArray ? cardIdArray : [];
        let piletoView = idsToView.map((id) => 
            gamestate["visibleCardsInGame"][id]
        );
        let cardStacks = piletoView.map((card) => 
            <Grid><CardStack key={card.cardId} gamestate={gamestate} anchor_id={card.cardId} openCardDetailsFunc={openCardDetailsFunc} /></Grid>
        );

        return(
            <Dialog open={isOpen} onClose={onCloseFunc}>
                <DialogTitle>Pile Details: {pileNameToFriendlyName(pileName)}</DialogTitle>
                        <Grid container spacing={2}>
                            {cardStacks}
                        </Grid>
            </Dialog>
        )
    }
    
    // failure case
    return(
        <Dialog open={isOpen} onClose={onCloseFunc}>
            <DialogTitle>Pile Details: N/A</DialogTitle>
        </Dialog>
    )
}