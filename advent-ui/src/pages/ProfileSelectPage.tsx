import { Box, Button, Stack, Typography, Paper } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import FestiveAppBar from "../components/FestiveAppBar";
import Snowfall from "../components/Snowfall";
import { PROFILES } from "../profiles";

export default function ProfileSelectPage() {
  return (
    <>
      <FestiveAppBar />
      <Snowfall density={80} speed={0.6} opacity={0.6} zIndex={0} />
      <Box
        sx={{
          p: 3,
          position: "relative",
          zIndex: 1,
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Paper
          sx={{
            p: 4,
            maxWidth: 500,
            textAlign: "center",
            background: "linear-gradient(180deg,#fff,#fff9f0)",
          }}
        >
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Welchen Adventskalender möchtest du öffnen? 🎄
          </Typography>
          <Typography variant="body1" sx={{ mb: 3 }}>
            Wähle Deinen Adventskalender.
          </Typography>

          <Stack spacing={2}>
            <Button component={RouterLink} to="/momo" variant="contained" size="large">
              {PROFILES.momo.label}
            </Button>
            <Button component={RouterLink} to="/trixi" variant="contained" size="large">
              {PROFILES.trixi.label}
            </Button>
            <Button component={RouterLink} to="/lulu" variant="contained" size="large">
              {PROFILES.lulu.label}
            </Button>
          </Stack>
        </Paper>
      </Box>
    </>
  );
}
