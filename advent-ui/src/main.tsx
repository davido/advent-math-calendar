import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { ThemeProvider, CssBaseline } from "@mui/material";
import { theme } from "./theme";
import CalendarPage from "./pages/CalendarPage";
import DoorPage from "./pages/DoorPage";
import ProfileSelectPage from "./pages/ProfileSelectPage";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <Routes>
          {/* Neue Auswahlseite */}
          <Route path="/" element={<ProfileSelectPage />} />

          {/* Profil-spezifische Kalender */}
          <Route path="/:slug" element={<CalendarPage />} />
          <Route path="/:slug/door/:day" element={<DoorPage />} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  </React.StrictMode>,
);
