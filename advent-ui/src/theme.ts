import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#b71c1c" },
    secondary: { main: "#1b5e20" },
    background: { default: "#fffaf3" },
  },
  shape: { borderRadius: 12 },
});
