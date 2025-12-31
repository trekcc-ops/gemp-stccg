import Box from "@mui/material/Box";
import TableLayout from './table-layout.jsx';
import Hand from './hand.jsx';
import ActionReactionPane from './action-reaction-pane.jsx';
import PhaseIndicator from './phase-indicator.jsx';

export default function MainLayoutGrid( {gamestate, sx} ) {
    return(
        <Box id="main-layout-grid">
            <TableLayout gamestate={gamestate} />
            <div id="hand-pane"><Hand gamestate={gamestate} /></div>
            <div id="action-reaction-pane"><ActionReactionPane gamestate={gamestate}/></div>
            <div id="phase-pane"><PhaseIndicator gamestate={gamestate} /></div>
        </Box>
    )
}