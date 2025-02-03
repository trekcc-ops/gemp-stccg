import Box from '@mui/material/Box';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import MobileStepper from '@mui/material/MobileStepper';
import Stack from '@mui/material/Stack';
import { Typography } from '@mui/material';

/*
const enum_to_friendly_text = new Map(
    ["SEED_DOORWAY", "Seed Doorway"],
    ["SEED_MISSION", "Seed Missions"]
    ["SEED_DILEMMA", "Seed Dilemmas"]
    ["SEED_FACILITY", "Seed Facility"]
    ["BETWEEN_TURNS", "Between Turns"]
    ["START_OF_TURN", "Start of Turn"]
    ["CARD_PLAY", "Card Play"]
    ["EXECUTE_ORDERS", "Execute Orders"]
    ["END_OF_TURN", "End of Turn"]
);
*/

const seed_steps = [
    "SEED_DOORWAY",
    "SEED_MISSION",
    "SEED_DILEMMA",
    "SEED_FACILITY"
]

const play_steps = [
    "BETWEEN_TURNS",
    "START_OF_TURN",
    "CARD_PLAY",
    "EXECUTE_ORDERS",
    "END_OF_TURN"
]

export default function PhaseIndicator( {gamestate} ) {
    let step_list;
    let active_step;

    if (seed_steps.indexOf(gamestate.currentPhase) != -1) {
        step_list = seed_steps;
        active_step = seed_steps.indexOf(gamestate.currentPhase);
    }
    else if (play_steps.indexOf(gamestate.currentPhase) != -1) {
        step_list = play_steps;
        active_step = play_steps.indexOf(gamestate.currentPhase);
    }
    else {
        step_list = ["Unknown phase"];
        active_step = 0;
    }

    return(
        /*
        <Box sx={{ width: '100%' }}>
            <Stepper activeStep={active_step} alternativeLabel>
                {step_list.map((label) => (
                <Step key={label}>
                    <StepLabel>{label}</StepLabel>
                </Step>
                ))}
            </Stepper>
        </Box>
        */
        <Stack>
            <MobileStepper
                variant="dots"
                steps={step_list.length}
                position="static"
                activeStep={active_step}
            />
            <Typography>{step_list[active_step]}</Typography>
        </Stack>
    );
}
