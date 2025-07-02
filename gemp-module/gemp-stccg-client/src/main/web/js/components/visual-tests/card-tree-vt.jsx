import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import CssBaseline from '@mui/material/CssBaseline';
import { theme } from '../../gemp-022/common.js';
import { ThemeProvider } from "@emotion/react";
import CardTree from "../card-tree.jsx";

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

root.render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <CardTree gamestate={gamestate} />
    </ThemeProvider>
  </StrictMode>
);
