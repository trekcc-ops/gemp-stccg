import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import CssBaseline from '@mui/material/CssBaseline';
import { theme } from '../../gemp-022/common.js';
import { ThemeProvider } from "@emotion/react";
import CardStack from "../card-stack.jsx";

const root = createRoot(document.getElementById("root"));

// Change this function to change the JSON input source.
function get_gamestate() {
    let request = new XMLHttpRequest();
    request.open("GET", "../../../player_state.json", false);
    request.send(null);
    let the_state = JSON.parse(request.responseText);
    return the_state;
}

let gamestate = get_gamestate();
let anchor_id = 1; // Romulan Shuttle

root.render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <CardStack gamestate={gamestate} anchor_id={anchor_id} />
    </ThemeProvider>
  </StrictMode>
);
