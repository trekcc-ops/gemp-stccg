import Box from '@mui/material/Box';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import MobileStepper from '@mui/material/MobileStepper';
import Stack from '@mui/material/Stack';
import { Typography } from '@mui/material';

const enum_to_friendly_text = new Map([
    ["SEED_DOORWAY", "Seed Doorway"],
    ["SEED_MISSION", "Seed Missions"],
    ["SEED_DILEMMA", "Seed Dilemmas"],
    ["SEED_FACILITY", "Seed Facility"],
    ["BETWEEN_TURNS", "Between Turns"],
    ["START_OF_TURN", "Start of Turn"],
    ["CARD_PLAY", "Card Play"],
    ["EXECUTE_ORDERS", "Execute Orders"],
    ["END_OF_TURN", "End of Turn"]
]);

function get_phase_data(gamestate) {
    let num_steps;
    let active_step;
    let friendly_name;

    let phases_in_order = gamestate["phasesInOrder"];
    let current_phase = gamestate["currentPhase"];

    if (phases_in_order === undefined || current_phase === undefined) {
        // missing data, hide UI entirely
        num_steps = 0;
        active_step = 0;
        friendly_name = "";
    }
    else {
        num_steps = phases_in_order.length;
        let step_index = phases_in_order.indexOf(current_phase);
        if (step_index === -1) {
            // phase-to-array mismatch, show dots but hide text
            active_step = 0;
            friendly_name = "";
        }
        else {
            // happy path
            active_step = step_index;
            friendly_name = enum_to_friendly_text.get(current_phase);
        }
    }

    return {
        "num_steps": num_steps,
        "active_step": active_step,
        "friendly_name": friendly_name
    }
}

export default function PhaseIndicator( {gamestate, sx} ) {
    let phase_data = get_phase_data(gamestate);

    return(
        <Box sx={{ width: '100%', ...sx }}>
            <Stepper activeStep={phase_data.active_step} alternativeLabel>
                {gamestate["phasesInOrder"].map((label) => (
                <Step key={label}>
                    <StepLabel>{enum_to_friendly_text.get(label)}</StepLabel>
                </Step>
                ))}
            </Stepper>
        </Box>
        /*
        <Stack sx={{...sx}}>
            <MobileStepper
                variant="dots"
                steps={phase_data.num_steps}
                position="static"
                activeStep={phase_data.active_step}
            />
            <Typography>{phase_data.friendly_name}</Typography>
        </Stack>
        */
    );
}
