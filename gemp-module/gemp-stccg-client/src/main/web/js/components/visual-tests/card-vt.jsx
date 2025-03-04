import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import CssBaseline from '@mui/material/CssBaseline';
import Card from "../card.jsx";
import { theme } from '../../gemp-022/common.js';
import { ThemeProvider } from "@emotion/react";
import { Stack, Typography } from "@mui/material";

const root = createRoot(document.getElementById("root"));

let dax = {
  "cardId": 55,
  "title": "Jadzia Dax",
  "blueprintId": "112_208",
  "owner": "andrew",
  "locationId": 7,
  "attachedToCardId": 48,
  "isStopped": false,
  "imageUrl": "https://www.trekcc.org/1e/cardimages/ds9/jadziadax.gif",
  "cardType": "PERSONNEL",
  "uniqueness": "UNIQUE"
}

let dax_stopped = {
  "cardId": 55,
  "title": "Jadzia Dax",
  "blueprintId": "112_208",
  "owner": "andrew",
  "locationId": 7,
  "attachedToCardId": 48,
  "isStopped": true,
  "imageUrl": "https://www.trekcc.org/1e/cardimages/ds9/jadziadax.gif",
  "cardType": "PERSONNEL",
  "uniqueness": "UNIQUE"
}

root.render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Stack sx={{width: "250px"}}>
        <Stack direction={"row"}>
            <Typography>Dax:</Typography>
            <Card card={dax} />
        </Stack>
        <Stack direction={"row"}>
            <Typography>Dax stopped:</Typography>
            <Card card={dax_stopped} /> 
        </Stack>
      </Stack>
    </ThemeProvider>
  </StrictMode>
);
