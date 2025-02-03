import React, { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import CssBaseline from '@mui/material/CssBaseline';
import FirstEditionGameLayout from "../components/1e-gamestate-layout";
import { theme } from '../../js/gemp-022/common.js';
import { ThemeProvider } from "@emotion/react";

const root = createRoot(document.getElementById("root"));
root.render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <FirstEditionGameLayout />
    </ThemeProvider>
  </StrictMode>
);
