import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import TableLayout from './table-layout.jsx';
import Hand from './hand.jsx';
import ActionReactionPane from './action-reaction-pane.jsx';
import PhaseIndicator from './phase-indicator.jsx';

export default function MainLayoutGrid( {gamestate, sx} ) {
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
            <TableLayout gamestate={gamestate} />
            
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
                    <Hand gamestate={gamestate} />
                </Box>

                <Box id="action-reaction-pane" sx={{
                    gridColumn: "right-side",
                    gridRow: "top-aligned",
                    borderLeft: "1px solid #fff",
                    borderTop: "1px solid #fff",
                    borderRight: "1px solid #fff"
                }}>
                    <ActionReactionPane gamestate={gamestate}/>
                </Box>
                
                <Box id="phase-pane" sx={{
                    gridColumn: "right-side",
                    gridRow: "mid-aligned",
                    border: "1px solid #fff"
                }}>
                    <PhaseIndicator gamestate={gamestate} />
                </Box>
            </Box>
            
        </Stack>
    )
}