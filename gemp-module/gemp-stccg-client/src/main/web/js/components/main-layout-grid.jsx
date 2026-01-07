import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import TableLayout from './table-layout.jsx';
import Hand from './hand.jsx';
import ActionReactionPane from './action-reaction-pane.jsx';
import PhaseIndicator from './phase-indicator.jsx';
import { useState } from "react";
import CardDetailsDialog from "./card-details-dialog.jsx";

export default function MainLayoutGrid( {gamestate, sx} ) {
    const [cardDetailsDialogBoxIsOpen, setCardDetailsDialogBoxIsOpen] = useState(false);
    const [cardDetailsCardId, setCardDetailsCardId] = useState(null);
    function openCardDetails(card_id) {
        setCardDetailsCardId(card_id);
        setCardDetailsDialogBoxIsOpen(true);
    }
    function closeCardDetails() {
        setCardDetailsDialogBoxIsOpen(false);
    }
    
    return(
        <Stack id="main-layout-grid"
            direction={"column"}
            sx={{
                /*display: "grid",
                gridTemplateColumns: "auto 240px",
                width: "100%",
                height: "100%",*/
                ...sx //also use incoming styles from parent
            }}
        >
            <TableLayout gamestate={gamestate} openCardDetailsFunc={openCardDetails} closeCardDetailsFunc={closeCardDetails} />
            
            <Box sx={{
                display: "grid",
                gridTemplateColumns: "[left-side] auto [right-side] 400px",
                gridTemplateRows: "[top-aligned] auto [mid-aligned] 1fr",
            }}>
                <Box id="hand-pane" sx={{
                    gridColumn: "left-side",
                    gridRow: "top-aligned / span 2",
                    borderTop: "1px solid #fff",
                    borderLeft: "1px solid #fff",
                    borderBottom: "1px solid #fff",
                }}>
                    <Hand gamestate={gamestate} openCardDetailsFunc={openCardDetails} closeCardDetailsFunc={closeCardDetails} />
                </Box>

                <Box id="action-reaction-pane" sx={{
                    gridColumn: "right-side",
                    gridRow: "top-aligned",
                    borderLeft: "1px solid #fff",
                    borderTop: "1px solid #fff",
                    borderRight: "1px solid #fff"
                }}>
                    <ActionReactionPane gamestate={gamestate} openCardDetailsFunc={openCardDetails} closeCardDetailsFunc={closeCardDetails} />
                </Box>
                
                <Box id="phase-pane" sx={{
                    gridColumn: "right-side",
                    gridRow: "mid-aligned",
                    border: "1px solid #fff"
                }}>
                    <PhaseIndicator gamestate={gamestate} openCardDetailsFunc={openCardDetails} closeCardDetailsFunc={closeCardDetails} />
                </Box>
            </Box>

            <CardDetailsDialog gamestate={gamestate} cardId={cardDetailsCardId} setCardIdFunc={setCardDetailsCardId} isOpen={cardDetailsDialogBoxIsOpen} onCloseFunc={closeCardDetails}></CardDetailsDialog>
            
        </Stack>
    )
}