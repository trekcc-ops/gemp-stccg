import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import CssBaseline from '@mui/material/CssBaseline';
import ActiveCardPane from "../active-card-pane.jsx";
import { theme } from '../../gemp-022/common.js';
import { ThemeProvider } from "@emotion/react";
import { Stack } from "@mui/material";

const root = createRoot(document.getElementById("root"));

let tomalok = {
    "cardId": 2,
    "title": "Tomalak",
    "blueprintId": "101_327",
    "owner": "andrew2",
    "zone": "ATTACHED",
    "locationId": 5,
    "affiliation": "ROMULAN",
    "attachedToCardId": 29,
    "isStopped": false,
    "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR327.jpg"
}

root.render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Stack>
        <ActiveCardPane />
        <ActiveCardPane card={tomalok} /> 
      </Stack>
    </ThemeProvider>
  </StrictMode>
);
