import { useState } from 'react';
import { theme } from './common.js';
import { ThemeProvider } from "@emotion/react";
import PlayerScorePane from '../components/player-score-pane.jsx';
import { get_your_player_id, get_opponent_player_id } from '../components/common.jsx';
import Stack from "@mui/material/Stack";
import PileDetailsDialog from '../components/pile-details-dialog.jsx';
import CardDetailsDialog from '../components/card-details-dialog.jsx';

export default function JQGameStateWrapper({gamestate}) {

    // Card Details Dialog
    const [cardDetailsDialogBoxIsOpen, setCardDetailsDialogBoxIsOpen] = useState(false);
    const [cardDetailsCardId, setCardDetailsCardId] = useState(null);
    function openCardDetails(card_id) {
        setCardDetailsCardId(card_id);
        setCardDetailsDialogBoxIsOpen(true);
    }
    function closeCardDetails() {
        setCardDetailsDialogBoxIsOpen(false);
    }

    // Pile Details Dialog
    const [pileDetailsDialogBoxIsOpen, setPileDetailsDialogBoxIsOpen] = useState(false);
    const [pileDetailsPlayerIdAndPileName, setPileDetailsPlayerIdAndPileName] = useState(null);
    function openPileDetails(playerAndPile) {
        setPileDetailsPlayerIdAndPileName(playerAndPile);
        setPileDetailsDialogBoxIsOpen(true);
    }
    function closePileDetails() {
        setPileDetailsDialogBoxIsOpen(false);
    }

    return(
        <ThemeProvider theme={theme}>
            <Stack>
                <PlayerScorePane id="opponent-player-score-pane" gamestate={gamestate} player_id={get_opponent_player_id(gamestate)} openPileDetailsFunc={openPileDetails} />
                <PlayerScorePane id="your-player-score-pane" gamestate={gamestate} player_id={get_your_player_id(gamestate)} openPileDetailsFunc={openPileDetails} />
            </Stack>
            <PileDetailsDialog gamestate={gamestate} pileDetailsPlayerIdAndPileName={pileDetailsPlayerIdAndPileName} isOpen={pileDetailsDialogBoxIsOpen} onCloseFunc={closePileDetails} openCardDetailsFunc={openCardDetails} />
            <CardDetailsDialog gamestate={gamestate} cardId={cardDetailsCardId} setCardIdFunc={setCardDetailsCardId} isOpen={cardDetailsDialogBoxIsOpen} onCloseFunc={closeCardDetails} />
        </ThemeProvider>
    )
}